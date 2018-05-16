(ns calva.v2.db
  (:require 
   [cljs.spec.alpha :as s]
   [kitchen-async.promise :as p]))

(s/def :calva/db
  map?)

(defonce *db
  (atom {}))


(defn- mutate!
  ([f]
   (mutate! *db f))
  ([*db f]
   (swap! *db (fn [db]
                (let [new-db (f db)]
                  (when-not (s/valid? :calva/db new-db)
                    (js/console.error (s/explain-str :calva/db new-db)))

                  new-db)))))


(defn transact! [f]
  (p/then (p/->promise (f @*db))
          (fn [new-db]
            (mutate! (fn [_]
                       new-db)))))