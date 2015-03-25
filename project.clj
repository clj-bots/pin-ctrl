(defproject clj-bots/pin-ctrl "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [slingshot "0.12.2"]
                 ; ## Here are dependencies specific to each of the individual implementations
                 ; rpi dependencies
                 ; bbb dependencies
                 ; onboard-common dependencies
                 [clj-gpio "0.1.0-SNAPSHOT"]
                 ; firmata dependencies
                 [clj-firmata "2.0.2-SNAPSHOT"]
                 ])
