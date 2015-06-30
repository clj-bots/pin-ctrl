(ns clj-bots.pin-ctrl.channels
  (:require [clj-bots.pin-ctrl.protocols :as pcp]
            [clojure.core.async :as async]))


(defn- chan?
  [c]
  (and (satisfies? clojure.core.async.impl.protocols/ReadPort c)
       (satisfies? clojure.core.async.impl.protocols/WritePort c)))

(defprotocol PChannelEdgeDetection

(defrecord EdgeChannelWrapper
  (get-edge-chan [board pin-n]
    (get-in @state-atom [:edge-channels pin-n]))
  (set-edge-chan! [board pin-n ch]
    (swap!
      @state-atom
      update-in
      (fn [old-chan] (async/close! old-chan) ch))))

