(ns clj-bots.pin-ctrl.implementation
  (:require '[slingshot.slingshot :refer [throw+]]))


(defonce implementations
  "A map of board-types to instantiation functions."
  (atom {}))

(defn instantiate
  [board-type config]
  (if-let [instantiator (board-type @implementations)]
    (instantiator config)
    (throw+ {:type ::missing-implementation
             :board-type board-type
             :message (str "No implementation for board type: " board-type ". "
                           "Check your spelling, or if this is an experimental library, "
                           "make sure you are registering the implementation correctly.")})))

(defn register-implementation
  "For implementations: call this function in your library somewhere with the board type keyword
  and a function for instantiating new board objects implementing the board protocols."
  [board-type board-instantiation-fn]
  (swap! implementations assoc board-type board-instantiation-fn))

