(defproject clj-bots/pin-ctrl "0.1.0-SNAPSHOT"
  :description "Abstraction layer for hardware programming"
  :url "http://github.com/clj-bots/pin-ctrl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-marginalia "0.8.0"]]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [slingshot "0.12.2"]
                 ; ## Here are dependencies specific to each of the individual implementations
                 ; rpi dependencies
                 ; bbb dependencies
                 ; onboard-gpio-common dependencies
                 [clj-gpio "0.1.0-SNAPSHOT"]
                 ; firmata dependencies
                 [clj-firmata "2.0.2-SNAPSHOT"]
                 ])
