(ns klarna-demo.core
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.periodic :as p]
            [clojure.string :as string]
            [schema.core :as s])
  (:gen-class))

(def ^:private base-uri "http://www.ncdc.noaa.gov/cdo-web/api/v2")
(def ^:private token "tvFfqDXOZiffOpXZMzuXSJufcBBaDbxL")

(def ^:private date-formatter (f/formatters :year-month-day))

(def ^:private location-types
  {::city "CITY"
   ::country "CNTRY"})

(def ^:private data-formatters
  {:input {:edn read-string
           :json json/parse-string}
   :output {:edn pr-str
            :json json/generate-string}})

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

(defn- get-data-no-caching
  ([uri] (get-data-no-caching uri nil))
  ([uri params]
   (let [{:keys [metadata results]} (get-json uri params)
         {:keys [count limit offset]} (:resultset metadata)]
     (if (> count (+ offset limit))
       (concat results
               (get-data-no-caching uri (merge params {:offset (+ offset limit)})))
       results))))

(def ^:private get-data (memoize get-data-no-caching))

(defn location-categories []
  (get-data "/locationcategories"))

(defn locations [type]
  (get-data "/locations"
            {:locationcategoryid (location-types type)
             :limit 1000}))

(defn- ->summary [acc m]
  (let [type (-> (:datatype m) string/lower-case keyword)]
    (merge acc {type (:value m)})))

(defn weather [id date]
  (->> (get-data "/data"
                 {:datasetid "GHCND"
                  :locationid id
                  :startdate date
                  :enddate date
                  :units "metric"})
       (group-by :station)
       (map (fn [[_ data]] (reduce ->summary {} data)))))

(defn- country-prefix [country]
  (let [id (->> (locations ::country)
                (filter #(= country (:name %)))
                first
                :id)
        code (some-> id
                     (string/split #":")
                     second)]
    (when code
      (str "CITY:" code))))

(defn daily-weather
  ([country date]
   (let [prefix (country-prefix country)
         city-ids (->> (locations ::city)
                       (filter #(string/starts-with? (:id %) prefix))
                       (map :id))]
     (->> city-ids
          (map (fn [id] [id (weather id date)]))
          (into {}))))
  ([country start-date end-date]
   (let [start-dt (f/parse date-formatter start-date)
         end-dt (f/parse date-formatter end-date)]
     (->> (p/periodic-seq start-dt (t/days 1))
          (take-while #(t/before? % end-dt))
          (map (fn [dt]
                 (let [date (f/unparse date-formatter dt)]
                   [date (daily-weather country date)])))
          (into {})))))

(defn write-data [f filename data]
  (spit filename (f data)))

(defn read-data [f filename]
  (f (slurp filename)))

(defn- date-between? [date start-date end-date]
  (let [[ts start-ts end-ts] (->> [date start-date end-date]
                                  (map #(f/parse date-formatter %)))
        interval (t/interval start-ts end-ts)]
    (t/within? interval ts)))

(defn -main
  [cmd start-date end-date filename & [data-format]]
  (let [cmd (keyword cmd)
        data-format (-> data-format (or "edn") keyword)]
    (case cmd
      :input
      (->> filename
           (read-data (get-in data-formatters [:input data-format]))
           (filter (fn [[date _]] (date-between? date start-date end-date)))
           (into {})
           ((get-in data-formatters [:output data-format]))
           println)

      :output
      (->> (daily-weather "Sweden" start-date end-date)
           (write-data (get-in data-formatters [:output data-format]) filename))

      (binding [*out* *err*]
        (println "Invalid command! Expecting 'input' or 'output'")))))
