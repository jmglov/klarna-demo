(ns klarna-demo.core
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.string :as string]
            [schema.core :as s])
  (:gen-class))

(def ^:private base-uri "http://www.ncdc.noaa.gov/cdo-web/api/v2")
(def ^:private token "tvFfqDXOZiffOpXZMzuXSJufcBBaDbxL")

(def ^:private location-types
  {::city "CITY"
   ::country "CNTRY"})

(defn- make-query [params]
  (some->> params
           (map (fn [[k v]] (str (name k) "=" v)))
           (string/join "&")
           (str "?")))

(s/defn ^:private get-json :- {:metadata {:resultset {:offset s/Int, :count s/Int, :limit s/Int}}
                               :results [s/Any]}
  [uri :- s/Str
   params :- (s/maybe {s/Keyword (s/either s/Str s/Int)})]
  (-> (str base-uri uri (make-query params))
      (http/get {:headers {:token token}})
      :body
      (json/parse-string true)))

(defn- get-data
  ([uri] (get-data uri nil))
  ([uri params]
   (let [{:keys [metadata results]} (get-json uri params)
         {:keys [count limit offset]} (:resultset metadata)]
     (println count limit offset)
     (if (> count (+ offset limit))
       (concat results (get-data uri (merge params {:offset (+ offset limit)})))
       results))))

(defn location-categories []
  (get-data "/locationcategories"))

(defn locations [type]
  (get-data "/locations"
            {:locationcategoryid (location-types type)
             :limit 1000}))

(defn weather [id date]
  (get-data "/data"
            {:datasetid "GHCND"
             :locationid id
             :startdate date
             :enddate date
             :units "metric"}))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
