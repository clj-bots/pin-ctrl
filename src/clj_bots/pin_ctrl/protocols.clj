(ns clj-bots.pin-ctrl.protocols)

(defprotocol PPinCtrlImplementation
  "Any implementation the pin-ctrl API must create an object which implements this protocol,
  and register it using the `clj-bots.pin-ctrl.implementation/register-implementation function."
  (create-board! [this config])
  (models [this])
  ; Let's add this so the simulator can effectively mock the configuration of pin availability and what not
  (default-config [this]))

(defprotocol PBoard
  "Basic board protocol, shared by any board, whether on board or over wire."
  ; XXX Not sure we need this actually; just let the implementation take care of it on it's own
  (pin-modes [this])
  (create-pin [this pin-n])
  (get-config [this])
  (swap-config! [this f]
    "This function should mutate board state in place, since existing pins will need to know the
    current state of the baord."))

; Need to have a good way of setting default nullary implementations of these
(defprotocol POverwireBoard
  "Overwire boards are boards that run over the wire, like Arduino boards over Firmata. This
  protocol is for functions specific to these boards."
  (reset-board! [this] "Overwire boards, such as arduino boards over firmata, can be reset!"))

(defprotocol PPinConfigure
  (set-mode! [this mode] "Set the mode of the pin, as long as it's supported by the pin's board."))

(defprotocol PReadablePin
  (read-value [this] "Read the binary or analog value of a pin. For analog input values this should be a normalized value between 0 and 1"))

(defprotocol PAinPin
  (read-raw-value [this] "Read the raw analog value of a pin. Maximum value depends on the number of bits ADC."))

(defprotocol PWriteablePin
  (write-value! [this val] "Set the value binary or analog value of a pin."))

(defprotocol PEdgeDetectablePin
  (set-edge! [this edge] "Set the edge of the pin (:rising, :falling, :both).")
  (create-edge-channel [this buffer] "Return a channel to be fed edge detection messages.")
  (release-edge-channels! [this] "Release (close) all edge channels on this pin."))


