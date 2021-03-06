(ns clj-bots.pin-ctrl.state-wrapper
  (:require [clj-bots.pin-ctrl.protocols :as pcp]
            [clojure.core.async :as async]))


;; We have this wrapper around the implementation boards. It also implements the board protocols, and defers
;; to the implementations for the juicy stuff. Really, the only job of this wrapper is to sanely handle state
;; _once_, so that individual implementations don't have to worry about it. The maintainence of state is
;; handled by a state-atom that is updated as the wrapped board does it's work.
;;
;; The state-atom should/can have the following keys:
;; {:modes "current mode settings for all the pins"
;;  :custom-mappings "so you can do nice names like :led1"
;;  :adapters "so you can create things like ain pins on pi, or software pwm; better drivers?"}
;;
;; We can also tease out any implementation specific config and pass that along.
;; Should these have to be registered by the implementations?
;;
;; This should also be the place we wrap things like validations so implementers don't have to worry about it.
;; So maybe this should be called something more general than `BoardWrapper`.

(defrecord BoardWrapper [state-atom impl-board]
  pcp/PBoard
  (init! [board]
    (pcp/init! impl-board)
    board)
  (available-pin-modes [_] (pcp/available-pin-modes impl-board))
  (pin-modes [_]
    ;; If pin-modes have been implemented, use that implementation
    (try
      (pcp/pin-modes impl-board)
      (catch Exception e
        (:pin-modes @state-atom))))

  pcp/POverwireBoard
  (reset-board! [_] (pcp/reset-board! impl-board))

  pcp/PPinConfigure
  (set-mode! [_ pin-n mode]
    (pcp/set-mode! impl-board pin-n mode)
    (swap! state-atom assoc pin-n mode))

  pcp/PStatefulPin
  (stateful-read-value [board pin-n]
    (pcp/read-value impl-board (get (pcp/pin-modes board) pin-n) pin-n))
  (stateful-write-value! [board pin-n val]
    (pcp/write-value! impl-board (get (pcp/pin-modes board) pin-n) pin-n val))

  pcp/PEdgeDetectablePin
  (set-edge! [_ pin-n edge f]
    (pcp/set-edge! impl-board pin-n edge f)))


(defn new-board-wrapper
  [impl-board]
  (BoardWrapper. (atom {}) impl-board))


