(ns clj-bots.pin-ctrl-rpi.implementation
  "# Raspberry Pi pin-ctrl API implementation"
  (:require [clj-bots.pin-ctrl
             [protocols :as pcp]
             [implementation :as impl]]
            [clj-bots.pin-ctrl :as pc]
            [gpio.core :as gpio]
            [clojure.core.async :as async :refer [chan >!! <!! go <! >! go-loop]]))



;; First we'll set up some function declarations and multi-method signatures that we'll be using in the implementations.

(declare writeable-pin? ok-val?)

(defmulti read-pin*
  "Inner method for reading from a pin; dispatches on pin mode"
  (fn [board pin-n mode] mode))

(defmulti unset-mode*
  "Inner method for unsetting a pin mode, in particular for being run before moving to another mode or turned off"
  (fn [board pin-n mode] mode))

(defmulti set-mode*
  "Inner method for setting the mode of a pin on the board"
  (fn [board pin-n mode] mode))

(defmulti write-value*
  "Inner method for writing a value to a pin"
  (fn [board pin-n mode val] mode))


;; Now let's hook these into the actual protocol implementations

(defrecord RPiBoard
  [edge-channels config]
  pcp/PBoard
  (available-pin-modes [_] (:pin-modes config))

  pcp/PPinConfigure
  (set-mode! [board pin-n mode]
    (set-mode* board pin-n mode))

  pcp/PReadablePin
  (read-value [board pin-n mode]
    (read-pin* board pin-n mode))

  pcp/PWriteablePin
  (write-value! [board pin-n mode val]
    (write-value* board pin-n val))

  pcp/PEdgeDetectablePin
  (set-edge! [board pin-n edge f]
    (let [[chan-port edge-chan]
          (or (get @edge-channels pin-n)
              (let [chan-port (gpio/open-channel-port pin-n)]
                [chan-port (gpio/create-edge-channel chan-port)]))]
      (if (= :none edge)
        (do (async/close! edge-chan)
            (gpio/close! chan-port)
            (swap! edge-channels dissoc pin-n))
        (do
          (gpio/set-direction! chan-port :in)
          (gpio/set-edge! chan-port edge)
          (go-loop []
            (when-let [value (<! edge-chan)]
              (f value)
              (recur))))))))


;; Now time to set up implementations for the multi-methods

(def memoized-open-port
  (memoize (fn [board pin-n]
             (let [n (get-in (:pin-modes board) [pin-n :gpio-pin])]
               (gpio/open-port n)))))

(defmethod read-pin* :input
  [board pin-n mode]
  (gpio/read-value (memoized-open-port board pin-n)))

(defmethod read-pin* :output
  [board pin-n mode]
  ;; Just defer to the :input method; gpio is generally fine with you trying to read fromj an output pin
  (read-pin* board pin-n :input))

(defmethod write-value* :output
  [board pin-n mode val]
  (gpio/write-value! (memoized-open-port board pin-n) val))

(defn- set-gpio-mode
  "This helper function takes care of some common logic for gpio :input and :output setting"
  [board pin-n direction]
  (let [p (memoized-open-port board pin-n)]
    (gpio/export! p)
    (gpio/set-direction! p direction)))

(defmethod set-mode* :input
  [board pin-n mode]
  (set-gpio-mode board pin-n :in))

(defmethod set-mode* :output
  [board pin-n mode]
  (set-gpio-mode board pin-n :out))

(defmethod unset-mode* :input
  [board pin-n mode]
  (gpio/unexport! (memoized-open-port board pin-n)))

(defmethod unset-mode* :output
  [board pin-n mode]
  (gpio/unexport! (memoized-open-port board pin-n)))


