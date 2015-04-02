(ns clj-bots.pin-ctrl
  "# Public API for pin-ctrl"
  (:require [clj-bots.pin-ctrl
             [protocols :as p]
             [implementation :as impl]]
            [clojure.core.async :as async :refer [>!! <!! >! <! go go-loop chan]]))


;; ## Board functions


(declare board? board-dispatch board-apply)


(defn create-board
  "Create a new board instance. For documentation on the available options, see the implementation
  library documentation." ; XXX - let's eventually register documentation for each of the implementations
  ([type]
   (create-board type {}))
  ([type config]
   (impl/instantiate type config)))

(defn get-config
  "Get board configuration."
  [board]
  (p/get-config board))

(defn update-config
  "Reset board configuration."
  [board f & args]
  (p/update-config board (fn [current-conf] (apply f current-conf args))))

(defn pin-modes
  "Returns a map of pin numbers to possible modes for the entire board, or the available modes of a specific pin."
  ; Should add second 1-arity that checks for 
  ([board]
   (p/pin-modes board))
  ([board pin]
   (get (pin-modes board) pin)))

(defn current-pin-modes
  "Returns a map of pin numbers to possible modes."
  ([board]
   (p/current-pin-modes board))
  ([board pin]
   (get (current-pin-modes board) pin)))

(defn set-default-board!
  "Set the default board implementation"
  [board]
  (println "Not yet implemented"))


;; ## Pin functions

(defrecord Pin
  [board pin-n])

(defn get-pin
  "Return a new pin object based on a given board, and optionally set the mode."
  ([board pin-n]
   (Pin. board pin-n))
  ([board pin-n mode]
   (p/set-mode! board pin-n mode)
   (Pin. board pin-n)))

(defn set-mode!
  "Set the mode of the pin. Must be a value supported by the pin."
  ([pin mode] (board-apply p/set-mode! pin mode))
  ([board pin-n mode] (p/set-mode! board pin-n mode)))

(defn read-value
  "Read a single value from the pin. If an analog input, returns a value between 0 and 1."
  ([pin] (board-apply p/read-value pin))
  ([board pin-n] (p/read-value board pin-n)))

(defn read-raw-value
  "Read a raw unprocessed value from an analog input pin."
  ([pin] (board-apply p/read-raw-value pin))
  ([board pin-n] (p/read-raw-value board pin-n)))

(defn write-value!
  "Write a value to a writable pin. The kind of value supported and what it means is entirely dependent on
  what kind of pin is being used."
  ([pin val] (board-apply p/write-value! pin val))
  ([board pin-n val] (p/write-value! board pin-n val)))

;; XXX Should we try to worry about race conditions here? Could make this lower level so read/write messages
;; don't have anything inbetween interfere.
(defn toggle!
  "Toggle a GPIO pin between high and low."
  ([pin] (board-apply pin toggle!))
  ([board pin-n]
   (let [current-val (read-value board pin-n)
         new-val (if (= current-val :low) :high :low)]
     (write-value! board pin-n new-val))))

;; ## Edge detection functionality

(defmulti set-edge!
  "Set the edge direction of a pin. Accepts `:falling`, `:rising`, `:both` and `:none` edges.
  Args should either be `[pin edge & [buffer]]` or `[board pin-n edge & [buffer]]`, where buffer is the size of
  the channel which will be tapped in the tapping functions (defaults to 1)."
  board-dispatch)

(defmethod set-edge! true
  [board pin-n edge & [buffer]]
  (p/set-edge! board pin-n edge (or buffer 1)))

(defmethod set-edge! false
  [pin edge & [buffer]]
  (board-apply p/set-edge! pin edge (or buffer 1)))

(defmulti create-edge-tap!
  "Create an edge tap for the given edge channel"
  board-dispatch)

(defmethod create-edge-tap! true
  [board pin-n & [buffer]]
  (async/tap (p/get-edge-mult board pin-n) (chan (or buffer 1))))

(defmethod create-edge-tap! false
  [pin [buffer]]
  (board-apply create-edge-tap! pin buffer))

(defn remove-edge-tap!
  "Untap edge channel tap"
  ([board pin-n tap-chan]
   (async/untap (p/get-edge-mult board pin-n) tap-chan))
  ([pin tap-chan]
   (board-apply remove-edge-tap! pin tap-chan)))


;; ### Some helper functions for navigating through boards and pins.

;; Nothing to see here...

(defn- board?
  [obj]
  (satisfies? p/PBoard obj))

(defn- board-dispatch
  [x & args] (board? x))

(defn- board-apply
  [f pin & args]
  (apply f (:board pin) (:pin-n pin) args))


