(ns clj-bots.pin-ctrl-simulator.implementation
  (:require [clj-bots.pin-ctrl
             [protocols :as pcp]
             [implementation :as impl]]
            [clojure.core.async :as async :refer [chan >!! <!! go <! >! go-loop]]))


;; This stuff really needs to be part of the core library

(def available-modes
  #{:input :output :ain :pwm})

;; Here we're going to declare some of the things we'll need in the implementation that we'd rather leave at
;; the end of this namespace for logical flow.

(declare current-pin-mode writeable-pin? ok-val?)

;; In addition to the standard protocol functions, we also need something which let's us set the state of
;; _input_ input pins for the purposes of simulation, since (for obvious reasons) this is not supported via
;; the standard protocols.

(defprotocol PSimControl
  "Protocol for simulation control functions"
  (set-state! [this pin-n val] "Set the state of a digital or analog input pin"))


;; And now the implementation.

(defrecord SimBoard
  [pin-state pin-modes edge-channels config]
  pcp/PBoard
  (init! [b] b)
  (available-pin-modes [_] (:pin-modes config))
  (pin-modes [_] @pin-modes)

  pcp/POverwireBoard
  (reset-board! [_]
    (if (= (:board-class config) :overwire)
      (do (reset! pin-modes)
          (reset! pin-state))
      (println "This option is not available for onboard boards")))

  pcp/PPinConfigure
  (set-mode! [_ pin-n mode]
    (swap! pin-modes assoc pin-n mode))

  pcp/PReadablePin
  (read-value [board pin-n mode]
    (get (deref (:pin-state board)) pin-n))

  pcp/PWriteablePin
  (write-value! [board pin-n mode val]
    (swap! pin-state assoc pin-n val))

  pcp/PEdgeDetectablePin
  (set-edge! [board pin-n edge ch]
    (let [match (case edge
                  (:none "edge")       #{}
                  (:rising "rising")   #{:high}
                  (:falling "falling") #{:low}
                  (:both "both")       #{:high :low})]
      (add-watch pin-state
                 ; May have to have board ids involved in this scheme for overwire; 
                 ; should have global registry of these board ids XXX
                 (symbol (str "edge-detection-watch-simulator" pin-n))
                 (fn [_ _ _ new-val]
                   (when (match new-val)
                     (>!! ch new-val))))))

  PSimControl
  (set-state! [board pin-n val]
    (assert (ok-val? board pin-n val)
            (str "The value " val " is not an acceptable value for pins of type " (current-pin-mode board pin-n)))
    (swap! pin-state assoc pin-n)))

;; Now we'll flesh out some of the various reading/writing functions

(defn pin-mode
  [board pin-n]
  (get (pcp/pin-modes board) pin-n))

(defn writeable-pin?
  [board pin-n]
  (#{:output :pwm} (pin-mode board pin-n)))

(defn ok-val?
  [board pin-n val]
  (case (current-pin-mode board pin-n)
    :output (#{0 1 \0 \1 :high :low} val)
    :pwm    #(and (<= 0 %) (>= 1 %))
    false))


;; How we create new boards

(defn sim-board
  [config]
  (SimBoard.
    (atom {})
    (atom {})
    (atom {})
    config))

(defn random-config
  [n-pins]
  {:pin-modes
   (into {}
         (for [i (range n-pins)]
           [[(rand-nth [:P8 :P9]) i]
            (filterv
              (fn [_] (> (rand) 0.68))
              available-modes)]))})


;; And register the implementation

(def implementation
  (reify
    Object
    (toString [_]
      "<BoardSimulator>")
    pcp/PPinCtrlImplementation
    (create-board [_ config]
      (sim-board config))
    (default-config [_]
      (pcp/default-config 100))))

(impl/register-implementation :simulator implementation)


