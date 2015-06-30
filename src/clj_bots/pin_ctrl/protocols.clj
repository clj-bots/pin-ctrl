(ns clj-bots.pin-ctrl.protocols
  "# Implementation protocols")

;; This namespace contains the protocols which a pin-ctrl implementation must satisy.
;; In particular, it must provide a single object satisfying the `PPinCtrlImplementation` protocol, and that
;; must have a `create-board` method which returns an object satisfying the remaining protocols.
;;
;; The objects satisfying these protocols must keep track of mode setting state, and in particular need to do
;; this so that they can know what underlying methods/functions they need to call in order to execute
;; writes/reads.
;; There is still some question about whether or not we should similarly keep track of config state.
;; This could be left up to the user, or could be handled in a similar manner to the mode state.

;; Notes on the configuration schema:
;;
;; * AIN (out?): Must have number of bits to translate raw to normalized


(defprotocol PPinCtrlImplementation
  "Any implementation the pin-ctrl API must create an object which implements this protocol,
  and register it using the `clj-bots.pin-ctrl.implementation/register-implementation function."
  (create-board [this config] "Return a new board instance of this implementation. This shouldn't do any initializaiton.")
  (default-config [this] "Return the default configuration map (see schema XXX) for this implementation. This is useful for simulation."))

;; read-raw-value -> analog-bits
;; remove-mult
;; updat set-edge!
;;
;; finshed:
;; read-value : arity taking mode
;; write-value : arity taking mode
;; added init!
;; changes: pin-modes -> available-modes
;; current-pin-modes -> pin-modes

;; # Board Implementation Protocols
;;
;; The following are the protocols for board implementations.
;; Those which are not required will be marked as such.

(defprotocol PBoard
  "Basic board protocol, shared by any board, whether on board or over wire."
  (init! [this] "Do any necessary initialization. Not required. Should return the board.")
  (available-pin-modes [this] "Return a map of pin numbers to available pin modes.")
  ;; This should now be optional, with the default calling through to the recorded state
  (pin-modes [this] "Get the current pin mode values. Implementing this method is optional; default behaviour is to let the state wrapper track modes and return from there. Only implement this method if you want to manually implement functions that directly query the board for a pin's state."))

; Need to have a good way of setting default nullary implementations of these
(defprotocol POverwireBoard
  "Overwire boards are boards that run over the wire, like Arduino boards over Firmata. This
  protocol is for functions specific to these boards."
  (reset-board! [this] "Overwire boards, such as arduino boards over firmata, can be reset!"))

(defprotocol PPinConfigure
  (set-mode! [board pin-n mode] "Set the mode of the pin, as long as it's supported by the pin's board."))

(defprotocol PReadablePin
  (read-value [board pin-n mode] "Read the binary or analog value of a pin with given mode. For gpio this should be :high or :low; for ain should be numeric between 0 and 1"))

(defprotocol PWriteablePin
  (write-value! [board pin-n mode val] "Set the binary or analog value of a pin; for analog, should be the raw, non-normalized value."))

(defprotocol PEdgeDetectablePin
  "Edge detection allows efficient detection of GPIO state changes (such as from a button press). User recieves this information via callback fn."
  (set-edge!
    [board pin-n edge f]
    "Set the direction of the edge detection on a GPIO input pin. Should call the callback function f with the current state of the pin. Each time the
    function is called, previous callback functions should no longer be run. State management here is up to user."))


;; # Stateful Board Wrapper Protocols
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; The following protocols are not to be used for implementations proper.
;; They are for implementations of stateful boards, which is orthogonal to an implementation which takes care
;; of how things run on one type of board vs another.
;; We may eventually open things up so that stateful board wrappers could

(defprotocol PStatefulPin
  "This Read and write without having to pass the mode"
  (stateful-read-value [board pin-n])
  (stateful-write-value! [board pin-n val]))

(defprotocol PChannelEdgeDetection
  (set-edge-chan! [board pin-n ch])
  (get-edge-chan [board pin-n]))

