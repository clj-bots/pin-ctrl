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

;; * + current-pin-modes

;; Notes on the configuration schema:
;;
;; * AIN (out?): Must have number of bits to translate raw to normalized


(defprotocol PPinCtrlImplementation
  "Any implementation the pin-ctrl API must create an object which implements this protocol,
  and register it using the `clj-bots.pin-ctrl.implementation/register-implementation function."
  (create-board [this config] "Return a new board instance of this implementation.")
  (default-config [this] "Return the default configuration map (see schema XXX) for this implementation. This is useful for simulation."))

(defprotocol PBoard
  "Basic board protocol, shared by any board, whether on board or over wire."
  (pin-modes [this] "Return a map of pin numbers to available pin modes.")
  (current-pin-modes [this] "Get the current pin mode values.")
  (get-config [this] "Return the configuration map of the board. For the schema see XXX.")
  (update-config [this f] "Run the given function on the existing configuration, and return a new board object with the resulting configuration."))

; Need to have a good way of setting default nullary implementations of these
(defprotocol POverwireBoard
  "Overwire boards are boards that run over the wire, like Arduino boards over Firmata. This
  protocol is for functions specific to these boards."
  (reset-board! [this] "Overwire boards, such as arduino boards over firmata, can be reset!"))

(defprotocol PPinConfigure
  (set-mode! [board pin-n mode] "Set the mode of the pin, as long as it's supported by the pin's board."))

(defprotocol PReadablePin
  (read-value [board pin-n] "Read the binary or analog value of a pin. For analog input values this should be a normalized value between 0 and 1"))

(defprotocol PAinPin
  (read-raw-value [board pin-n] "Read the raw analog value of a pin. Maximum value depends on the number of bits ADC."))

(defprotocol PWriteablePin
  (write-value! [board pin-n val] "Set the binary or analog value of a pin."))

(defprotocol PEdgeDetectablePin
  "Edge detection allows efficient detection of GPIO state changes (such as from a button press).
  These changes are exposed to the user via `core.async` channels, which can be created via this
  protocol. Configuration is done via set-mode!"
  (set-edge! [board pin-n edge] "Set the direction of the edge detection on a GPIO pin")
  (create-edge-channel [board pin-n buffer] "Return a channel to be fed edge detection messages.")
  (release-edge-channels! [board pin-n] "Release (close) all edge channels on this pin."))


