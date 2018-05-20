(ns calva.v2.db
  (:require
   [cljs.spec.alpha :as s]))

(s/def :calva/db
  map?)


(defn- mutate! [*db f]
  (swap! *db (fn [db]
                (let [new-db (f db)]
                  (when-not (s/valid? :calva/db new-db)
                    (js/console.error (s/explain-str :calva/db new-db)))

                  new-db))))
