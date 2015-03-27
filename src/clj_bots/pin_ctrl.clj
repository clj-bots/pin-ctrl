(ns clj-bots.pin-ctrl
  "# Public API for pin-ctrl"
  (:require [clj-bots.pin-ctrl
             [protocols :as p]
             [implementation :as impl]]))


;; ## Board functions

(defn create-board!
  "Create a new board instance. For documentation on the available options, see the implementation
  library documentation." ; XXX - let's eventually register documentation for each of the implementations
  ([type]
   (create-board! type {}))
  ([type config]
   (impl/instantiate type config)))

(defn get-config
  "Get board configuration."
  [board]
  (p/get-config board))

(defn swap-config!
  "Reset board configuration."
  [board f & args]
  (p/swap-config! board (fn [current-conf] (apply f current-conf args))))

(defn pin-modes
  "Returns a map of pin numbers to possible modes."
  ([board]
   (p/pin-modes board))
  ([board pin]
   (get (pin-modes board) pin)))

(defn set-default-board!
  "Set the default board implementation"
  [board])


;; ## Pin functions

(defrecord Pin
  [board pin-n])

(defn- board?
  [obj]
  (satisfies? p/PBoard obj))

(defn- board-apply
  [f pin & args]
  (apply f (:board pin) (:pin-n pin) args))


(defn get-pin
  "Return a new pin object based on a given board, and optionally set the mode."
  ([board pin-n]
   (Pin. board pin-n))
  ([board pin-n mode]
   (p/set-mode! board pin-n mode)
   (Pin. board pin-n)))

(defn set-mode!
  "Set the mode of the pin. Must be a value supported by the pin."
  ([pin mode]
   (board-apply p/set-mode! pin mode))
  ([board pin-n mode]
   (p/set-mode! board pin-n mode)))

(defn read-value
  "Read a single value from the pin. If an analog input, returns a value between 0 and 1."
  ([pin]
   (board-apply p/read-value pin))
  ([board pin-n]
   (p/read-value board pin-n)))

(defn read-raw-value
  "Read a raw unprocessed value from an analog input pin."
  ([pin]
   (board-apply p/read-raw-value pin))
  ([board pin-n]
   (p/read-raw-value board pin-n)))

(defn write-value!
  "Write a value to a writable pin. The kind of value supported and what it means is entirely dependent on
  what kind of pin is being used."
  ([pin val]
   (board-apply p/write-value! pin val))
  ([board pin-n val]
   (p/write-value! board pin-n val)))

;; ### Edge detection functionality

(defn set-edge!
  "Set the edge direction of a pin. Accepts `:falling`, `:rising` and `:both`."
  ([pin edge]
   (board-apply p/set-edge! pin edge))
  ([board pin-n edge]
   (p/set-edge! board pin-n edge)))

(defmulti create-edge-channel
  "Return a channel on which events will be published. Buffer defaults to 1:
  `([board pin-n & [buffer]] [pin & [buffer]])`"
  (fn [& args] (board? (first args))))

(defmethod create-edge-channel false
  [pin & [buffer]]
  (board-apply p/create-edge-channel pin (or buffer 1)))

(defmethod create-edge-channel true
  [board pin-n & [buffer]]
  (p/create-edge-channel board pin-n (or buffer 1)))

(defn release-edge-channels!
  "Release the edge channel, closing all channels tap channels that have been created."
  ([pin] (board-apply p/release-edge-channels! pin))
  ([board pin-n] (p/release-edge-channels! board pin-n)))

