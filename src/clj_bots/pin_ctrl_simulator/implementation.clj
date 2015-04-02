(ns clj-bots.pin-ctrl-simulator.implementation
  (:require [clj-bots.pin-ctrl
             [protocols :as pcp]
             [implementation :as impl]]
            [clojure.core.async :as async :refer [chan >!! <!! go <! >! go-loop]]))

(def full-available-modes
  #{:input :output :input-rising :input-falling :input-both :ain :aout :pwm})

(def available-modes
  #{:gpio :ain :aout :pwm})

(def mode-mapping
  {:gpio [:input :output :input-rising :input-falling :input-both]
   :ain  [:ain]
   :aout [:aout]
   :pwm  [:pwm]})

(declare current-pin-modes)

(defmulti read-pin*
  "Inner method for reading from a pin; dispatches on pin mode"
  (fn [board pin-n]
    (get (current-pin-modes board) pin-n)))

(declare current-pin-mode writeable-pin? ok-val?)

(defrecord SimBoard
  [pin-state pin-modes edge-channels config]
  pcp/PBoard
  (pin-modes [_] (:pin-modes config))
  (current-pin-modes [_] @pin-modes)
  (get-config [_] config)
  (update-config [this f] (update-in this [:config] f))

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
  (read-value [board pin-n]
    (read-pin* board pin-n))

  pcp/PAinPin
  (read-raw-value [board pin-n]
    (assert (= (current-pin-mode board pin-n) :ain)
            "Only :ain pins have raw values")
    (get @pin-state pin-n))

  pcp/PWriteablePin
  (write-value! [board pin-n val]
    (assert (writeable-pin? board pin-n)
            (str "Pins of mode " (current-pin-mode board pin-n) " are not writeable"))
    (assert (ok-val? board pin-n val)
            (str "The value " val " is not an acceptable value for pins of type " (current-pin-mode board pin-n)))
    (swap! pin-state pin-n val))

  pcp/PEdgeDetectablePin
  (set-edge! [board pin-n edge buffer]
    (if (= :none edge)
      (swap! edge-channels dissoc pin-n)
      (let [[c m] (or (get @edge-channels pin-n)
                      (let [c (chan buffer)]
                        [c (async/mult c)]))
            match (case edge
                    (:rising "rising")   #{:high}
                    (:falling "falling") #{:low}
                    (:both "both")       #{:high :low})]
        (swap! edge-channels assoc pin-n [c m]
        (add-watch pin-state
                   ; May have to have board ids involved in this scheme for overwire; 
                   ; should have global registry of these board ids XXX
                   (symbol (str "edge-detection-watch-simulator" pin-n))
                   (fn [_ _ _ new-val]
                     (when (match new-val)
                       (>!! c new-val))))))))
  (get-edge-mult [board pin-n]
    (second (get @edge-channels pin-n))))

(defmethod read-pin* :input
  [board pin-n]
  (get (deref (:pin-state board)) pin-n))

(defmethod read-pin* :output
  [board pin-n]
  (get (deref (:pin-state board)) pin-n))

(defn get-bits
  [board pin-n]
  (get-in board [:config :analog-bits pin-n]))

(defmethod read-pin* :ain
  [board pin-n]
  (double (/ (get (deref (:pin-state board)) pin-n)
             (get-bits board pin-n))))


(defn current-pin-mode
  [board pin-n]
  (get (current-pin-modes board) pin-n))

(defn writeable-pin?
  [board pin-n]
  (#{:output :aout} (current-pin-mode board pin-n)))

(defn ok-val?
  [board pin-n val]
  (case (current-pin-mode board pin-n)
    :output (#{0 1 \0 \1 :high :low} val)
    :aout   (fn [n]
              (and (integer? n)
                   (>= n 0)
                   (< (Math/pow 2 (get-bits board pin-n)))))
    false))

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


