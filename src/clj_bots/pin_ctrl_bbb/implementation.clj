(ns clj-bots.pin-ctrl-bbb.implementation
  (:require [gpio.core :as gpio]
            [clj-bots.pin-ctrl
             [protocols :as pcp]
             [implementation :as impl]]
            [clojure.java.io :as io]))


(defrecord Board [pin-mapping config]
  pcp/PBoard
  (pin-modes [_]
    {:todo :XXX})
  (get-config [this]
    config)
  ; XXX - Hmmm... do we actually need this?
  (update-config [this f]
    (swap! config f)))


(let [inner-fn
      (memoize
        (fn []
          (read-string (slurp (clojure.java.io/resource "bbb-pin-mappings.edn")))))]
  ; XXX - Hmm; should call this something else either here or in the Implementation protocol
  (defn- get-pin-mappings
    "Get pin mappings from configuration edn file"
    []
    ((inner-fn))))


(defn new-board
  [config]
  (Board. (get-pin-mappings) config))


(def implementation
  (reify
    Object
    (toString [_]
      "<BeagleBoneBlackImplementation>")
    pcp/PPinCtrlImplementation
    (create-board [_ config]
      (Board. (get-pin-mappings) config))
    (default-config [_]
      (get-pin-mappings))))

;; This makes it possible to register the implementation by calling this function

(defn register-implementation
  ([impl-key]
   (impl/register-implementation impl-key implementation))
  ([]
   (register-implementation :bbb)))

;; Having this here means the implementation will get registered if this namespace is required

(register-implementation)

:OK

