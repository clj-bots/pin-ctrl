(ns clj-bots.pin-ctrl.implementation
  (:require [slingshot.slingshot :refer [throw+]]
            [clj-bots.pin-ctrl.protocols :as p]))


(defonce implementations
  (atom
    (with-meta
      {}
      {:doc "A map of board-type keys to implementation objects (that is, implementations of p/PPinCtrlImplementation)."})))

(defn instantiate
  [board-type config]
  (if-let [implementation (get @implementations board-type)]
    (p/create-board! implementation config)
    (throw+ {:type ::missing-implementation
             :board-type board-type
             :message (str "No implementation for board type: " board-type ". "
                           "Check your spelling, or if this is an experimental library, "
                           "make sure you are registering the implementation correctly.")})))

(defn register-implementation
  "For implementations: call this function in your library somewhere with the board type keyword
  and a function for instantiating new board objects implementing the board protocols."
  [board-type implementation]
  (swap! implementations assoc board-type implementation))

