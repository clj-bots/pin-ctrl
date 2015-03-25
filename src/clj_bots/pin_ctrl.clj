(ns clj-bots.pin-ctrl
  "# Public API for pin-ctrl"
  (:require '[clj-bots.pin-ctrl
              [protocols :as p]
              [implementation :as impl]]))


;; ## Board functions

(defn create-board
  "Create a new board instance. For documentation on the available options, see the implementation
  library documentation." ; XXX - let's eventually register documentation for each of the implementations
  ([type]
   (create-board type config))
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

(defn create-pin
  "Return a new pin object based on a given board, and optionally set the mode."
  ([board pin-n]
   (p/create-pin pin-n))
  ([board pin-n mode]
   (let [p (p/create-pin pin-n)]
     (p/set-mod! p mode)
     p)))

