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
  pc/pin-mode)

(defmulti set-mode*
  "Inner method for setting the mode of a pin on the board"
  (fn [board pin-n mode] mode))

(defmulti write-value*
  "Inner method for writing a value to a pin"
  (fn [board pin-n mode val] mode))


(defrecord RPiBoard
  [edge-channels config]
  pcp/PBoard
  (available-pin-modes [_] (:pin-modes config))

  pcp/PPinConfigure
  (set-mode! [board pin-n mode]
    (unset-mode* board pin-n)
    (set-mode* board pin-n mode))

  pcp/PReadablePin
  (read-value [board pin-n mode]
    (read-pin* board pin-n mode))

  pcp/PWriteablePin
  (write-value! [board pin-n mode val]
    (write-value* board pin-n val))

  ;pcp/PEdgeDetectablePin
  ;(set-edge! [board pin-n edge buffer]
    ;(gpio/set-edge! pin-n edge)
    ;(let [[c m] (or (get @edge-channels pin-n)
                    ;)]
      ;(if (= :none edge)
        ;(do (async/close! c)
            ;(swap! edge-channels dissoc pin-n))
        ;(let [p (open-channel-port 
                  ;c (create-edge-channel pin-n)


      ;(do (async/close! (@edge-channels pin-n))
          ;(swap! edge-channels dissoc pin-n))
                      ;(let [c (chan buffer)]
                        ;[c (async/mult c)]))
            ;(

  )



