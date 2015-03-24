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

(defn set-config!
  "Reset board configuration."
  [board config]
  (p/set-config! board config))

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
  "Return a new pin object based on a given board"
  [board pin-n])



