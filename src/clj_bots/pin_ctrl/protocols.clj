(ns clj-bots.pin-ctrl.protocols)

(defprotocol PPinCtrlImplementation
  "Any implementation the pin-ctrl API must create an object which implements this protocol,
  and register it using the `clj-bots.pin-ctrl.implementation/register-implementation function."
  (create-board! [this config])
  (models [this]))

(defprotocol PBoard
  "Basic board protocol, shared by any board, whether on board or over wire."
  (map-pin [this pinspec]
    "Some devices, such as the Beaglebone black, have non-trivial mappings between convenient
    `[header, pin-number]` pairs to the underlying fs-gpio pins.")
  (pin-modes [this])
  (create-pin [this pin-n])
  (get-config [this])
  (set-config! [this config]))

; Need to have a good way of setting default nullary implementations of these
(defprotocol POverwireBoard
  "Overwire boards are boards that run over the wire, like Arduino boards over Firmata. This
  protocol is for functions specific to these boards."
  (reset-board! [this] "Overwire boards, such as arduino boards over firmata, can be reset!")
  ; Not sure we really need this one to be separate from above
  (get-config! [this] "Load the configuratino settings of the board over the wire directly from the device and cache."))

(defprotocol PPinConfigure
  (set-mode! [this mode] "Set the mode of the pin, as long as it's supported by the pin's board."))

(defprotocol PReadablePin
  (read-value [this] "Read the binary or analog value of a pin."))

(defprotocol PWriteablePin
  (write-value! [this val] "Set the value binary or analog value of a pin."))

(defprotocol PEdgeDetectablePin
  (set-edge! [this edge] "Set the edge of the pin (:rising, :falling, :both).")
  (create-edge-channel [this] "Return a channel to be fed edge detection messages.")
  (release-edge-channel! [this] "Release the edge channel."))


