(ns clj-bots.pin-ctrl-rpi.implementation
  "# Raspberry Pi pin-ctrl API implementation"
  (:require [clj-bots.pin-ctrl
             [protocols :as pcp]
             [implementation :as impl]]
            [clj-bots.pin-ctrl :as pc]
            [clj-gpio :as gpio]
            [clojure.core.async :as async :refer [chan >!! <!! go <! >! go-loop]]))



;; First we'll set up some function declarations and multi-method signatures that we'll be using in the implementations.

(declare current-pin-mode writeable-pin? ok-val?)

(defmulti read-pin*
  "Inner method for reading from a pin; dispatches on pin mode"
  pc/current-pin-mode)

(defmulti unset-mode*
  "Inner method for unsetting a pin mode, in particular for being run before moving to another mode or turned off"
  pc/current-pin-mode)

(defmulti set-mode*
  "Inner method for setting the mode of a pin on the board"
  (fn [board pin-n mode] mode))

(defmulti write-value*
  "Inner method for writing a value to a pin"
  (fn [board pin-n val]
    (pc/current-pin-mode board pin-n)))


(defrecord RPiBoard
  [pin-modes edge-channels config]
  pcp/PBoard
  (pin-modes [_] (:pin-modes config))
  (current-pin-modes [_] @pin-modes)
  (get-config [_] config)
  (update-config [this f] (update-in this [:config] f))

  pcp/PPinConfigure
  (set-mode! [board pin-n mode]
    (unset-mode* board pin-n)
    (set-mode* board pin-n mode)
    (swap! pin-modes assoc pin-n mode))

  pcp/PReadablePin
  (read-value [board pin-n]
    (read-pin* board pin-n))

  pcp/PWriteablePin
  (write-value! [board pin-n val]
    (write-value* board pin-n val))

  pcp/PEdgeDetectablePin
  (set-edge! [board pin-n edge buffer]
    (gpio/set-edge! pin-n edge)
    (let [[c m] (or (get @edge-channels pin-n)
                    )]
      (if (= :none edge)
        (do (async/close! c)
            (swap! edge-channels dissoc pin-n))
        (let [p (open-channel-port 
                  c (create-edge-channel pin-n)


      (do (async/close! (@edge-channels pin-n))
          (swap! edge-channels dissoc pin-n))
                      (let [c (chan buffer)]
                        [c (async/mult c)]))
            (



