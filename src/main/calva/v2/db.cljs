(ns calva.v2.db
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require 
   [cljs.spec.alpha :as s]
   [cljs.core.async :as async :refer [<! >! chan close! put!]]
   [kitchen-async.promise :as p]))

(s/def :calva/db
  map?)


(def ^:private mut
  (async/chan))


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
  (async/put! mut f))


(go (while true
      (let [f (async/<! mut)]
        (js/console.log "Got a message")
        (p/then (p/->promise (f @*db))
          (fn [new-db]
            (mutate! (fn [_]
                       new-db))
            
            (js/console.log "Done with message"))))))