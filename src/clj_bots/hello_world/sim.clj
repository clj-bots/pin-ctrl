(ns clj-bots.hello-world.sim
  "# test of the simulator implementation"
  (:require [clj-bots.pin-ctrl :as pc]
            [clj-bots.pin-ctrl-simulator.implementation :as sim]))

;; Create a simulator board, vaguely modelling a small subset of a BBB's pins.
(def board
  (pc/create-board :simulator
                   {:board-class :onboard ; not too important yet
                    :pin-modes {[:P8 14] [:gpio :ain]
                                [:P8 15] [:ain :aout]
                                [:P8 16] [:gpio]}
                    :analog-bits {[:P8 14] 8
                                  [:P8 15] 8}}))

;; Now let's say we want to initialize pin 14 on header :P8 as a GPIO pin, and use it to control an LED that we'll blink.
(def pin-n [:P8 14])
(def pin (pc/get-pin board pin-n))
(pc/set-mode! pin :output)


;; Now let's set up some blinking
(def blink-thread
  (future
    (loop []
      (pc/toggle! pin)
      (Thread/sleep 1000)
      (recur))))

;; And while our write loop is running, let's monitor the state of the pin
(let [interval 200
      duration 10000
      steps    (/ duration interval)]
  (doseq [i (range steps)]
    (Thread/sleep 200)
    (println "Value at" (* i interval) (pc/read-value pin))))

;; Now that we're done, let's close up shop.
(future-cancel blink-thread)
(pc/set-mode! pin :off)

