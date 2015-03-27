(ns clj-bots.hello-world.custom-mappings
  "# Demonstrating pin-ctrl's custom pin mappings"
  (:require [clj-bots.pin-ctrl :as pc]
            [clj-bots.pin-ctrl-rpi as rpi]
            [clj-bots.pin-ctrl-bbb as bbb]))

;; This feature was envisioned as a way of supporting cross device deployment.
;; The idea is that we would like a way of referring to specific pins abstractly, leaving the details of which
;; physical pin should be used on any given device up to the configuration of the user.

;; Say we want to fire off an LED and use GPIO pin `1` on the Pi, but but `[:P8 14]` on the BBB.
;; First, let's create a configuration map specifying all this.
(def config
  ; Here are our BBB specific configurations
  {:bbb
   {:custom-mappings
    {:led [:P8 14]}}
   ; And now for the RPi
   :rpi
   {:custom-mappings
    {:led 1}}})

;; Now we can simply use an environment variable to decide which kind of board we're using, and
;; subsequently, which pin configurations we'll be using.
(def board-type (symbol (get (System/getenv) "BOARD_TYPE")))

;; This should be either `:bbb` or `:rpi`

;; Now we can create the board with this configuration.
(def board (create-board! board-type (config board-type)))

;; Now that we've loaded our board with custom mappings, blinking the correct LED is agnostic to the
;; underlying board.
(doseq [i (range 10)]
  (write-value! board :led :high)
  (Thread/sleep 500)
  (write-value! board :led :high)
  (Thread/sleep 500))

;; In the code above, when we specify `:led` as the `pin-n` argument, it looks up the `:custom-mappings`
;; configuration we specified, and uses that to decide which actual pins to instantiate.
;; For this to make sense of course, there should be no key duplications between the keys of the
;; `:custom-mappings` and the standard pin mappings, which we should enforce in the API code.

