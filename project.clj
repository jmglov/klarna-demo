(defproject klarna-demo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [cheshire "5.6.1"]
                 [clj-http "2.2.0"]
                 [prismatic/schema "1.1.1"]]
  :main ^:skip-aot klarna-demo.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
