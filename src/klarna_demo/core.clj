(ns klarna-demo.core
  (:require [cheshire.core :as json]
            [clj-http.client :as http])
  (:gen-class))

(defn location-categories []
  (-> (http/get "http://www.ncdc.noaa.gov/cdo-web/api/v2/locationcategories"
                {:headers {:token "tvFfqDXOZiffOpXZMzuXSJufcBBaDbxL"}})
      :body
      (json/parse-string true)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
