(ns clj-bots.pin-ctrl
  "# Public API for pin-ctrl"
  (:require [clj-bots.pin-ctrl
             [protocols :as p]
             [implementation :as impl]
             [state-wrapper :as sw]]
            [clojure.core.async :as async :refer [>!! <!! >! <! go go-loop chan]]))


;; The philosophy behind pin-ctrl is that physical computing shouldn't require different API's for different
;; devices.
;; A set of routines involving monitoring inputs, and controlling outputs should be independent of what device
;; it runs on, within the bounds of the capabilities and features of those devices.
;;
;; For this reason, pin-ctrl aims to establish itself as a common device programming API, with multiple
;; implementations available for different devices.
;; These implementations are separate libraries depending on the core pin-ctrl library which implement the
;; underlying protocols.


;; ## Board functions
;;
;; The starting point for this library is the `board` abstraction.
;; Board objects carry with them information about what pins are available, what modes are available for those
;; pins, and the current mode settings for those pins.


(declare board? board-dispatch board-apply)

;; We start by creating board objects with the `create-board` function, which returns a board object based
;; on the specified `board-type`, which points towards the implementation key for the device being used (e.g.
;; `:rpi`, `:bbb`, `:firmata`, etc.).
;; The full set of configuration options will be detailed elsewhere soon (XXX Todo).

(defn create-board
  "Create a new board instance. For documentation on the available options, see the implementation
  library documentation." ; XXX - let's eventually register documentation for each of the implementations
  ([type]
   (create-board type {}))
  ([type config]
   (p/init! 
     (sw/new-board-wrapper (impl/instantiate type config)))))


;; What follows then is a suite of simple tools for dealing with the boards.

(defn pin-modes
  "Returns a map of pin numbers to possible modes for the entire board, or the available modes of a specific pin."
  [board]
  (p/pin-modes board))

(defn pin-mode
  "Return the current mode of a specific pin, either as a pin object or a board pin-n combo."
  ([board pin-n]
   (get (pin-modes board) pin-n))
  ([pin]
   (board-apply pin-mode pin)))


(defmulti available-pin-modes 
  "Returns the available pin modes for a board object (as a map of pin-numbers to modes), or if a pin object or board
  pin-n combination is passsed, directly returns a collection of the available modes for that pin."
  board-dispatch)

(defmethod available-pin-modes true
  ([board]
   (p/available-pin-modes board))
  ([board pin-n]
   (get (available-pin-modes board) pin-n)))

(defmethod available-pin-modes false
  [pin]
  (board-apply available-pin-modes pin))

;; I'm actually not sure if this is a good idea or not.
;; Given how much other dispatching there is going on, this would complicate things, and it would also make
;; basically every function in the api a defmethod, which is a little annoying for docstrings.

(defn set-default-board!
  "Set the default board implementation"
  [board]
  (println "Not yet implemented"))


;; ## Pin functions

;; The following functions are provided for controlling and monitoring pin states.

;; In addition to the board abstraction, we offer the Pin abstraction for dealing with the control and state
;; of individual pins.
;; This abstraction is "thin", in that it is really just a reference to a board and pin number.
;; In the functions that follow, you will see that the pin control/access functions can typically operate
;; either directly on the board given a `pin-n`, or on a pin abstraction.
;; Whether you use the pin abstraction or the board objects directly is up to you.


(defrecord Pin
  [board pin-n])

(defn get-pin
  "Return a new pin object based on a given board, and optionally set the mode."
  ([board pin-n]
   (Pin. board pin-n))
  ([board pin-n mode]
   (p/set-mode! board pin-n mode)
   (Pin. board pin-n)))

;; Many pins have multiple available modes.
;; This function lets us switch between modes, when possible, and activate modes that are not activated by
;; default, such as GPIO pins.

(defn set-mode!
  "Set the mode of the pin. Must be a value supported by the pin."
  ([pin mode] (board-apply p/set-mode! pin mode))
  ([board pin-n mode] (p/set-mode! board pin-n mode)))

;; Next we have our reader function.

(defn read-value
  "Read a single value from the pin. If an analog input, returns a value between 0 and 1."
  ([pin] (board-apply p/stateful-read-value pin))
  ([board pin-n] (p/stateful-read-value board pin-n)))

;; And last but not least, control functions:

(defn write-value!
  "Write a value to a writable pin. The kind of value supported and what it means is entirely dependent on
  what kind of pin is being used."
  ([pin val] (board-apply p/stateful-write-value! pin val))
  ([board pin-n val] (p/stateful-write-value! board pin-n val)))

;; XXX Should we try to worry about race conditions here? Could make this lower level so read/write messages
;; don't have anything inbetween interfere.

(defn toggle!
  "Toggle a GPIO pin between high and low."
  ([pin] (board-apply toggle! pin))
  ([board pin-n]
   (let [current-val (read-value board pin-n)
         new-val (if (= current-val :low) :high :low)]
     (write-value! board pin-n new-val))))


;; ## Edge detection functionality
;;
;; Certain applications, such as monitoring of a button push or other input trigger, require consistent
;; monitoring of a pin value.
;; The naive tight-loop approach here can be quite inefficient.
;; Fortunately, GPIO pins typically support edge detection functionality, which allows the system to
;; efficiently monitor for changes in pin state, and send those changes as interrupts within the program.
;;
;; This library exposes these interrupts -- naturally -- via core.async channels.
;; The usage pattern involves first setting the edge detection mode of the pin, then tapping into a central
;; channel and using that tap to convey messages of state changes throughout the app.

(defn set-edge!
  ([board pin-n edge f]
   (p/set-edge! board pin-n edge f))
  ([pin edge f]
   (board-apply p/set-edge! pin edge f)))


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

