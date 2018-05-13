(ns calva.v2.cmd
  (:require [calva.v2.db :refer [*db]]
            [calva.repl.nrepl :as nrepl]))

(defn ^{:cmd "calva.v2.connect"} connect []
  (let [^js socket (nrepl/connect #js {:host "localhost"
                                       :port "64772"
                                       :on-connect #(swap! *db
                                                           (fn [m1 m2]
                                                             (merge-with merge m1 m2))
                                                           {:conn {:connected? true}})
                                       :on-end #(swap! *db (fn [m]
                                                             (let [^js socket (get-in m [:conn :socket])]
                                                               (-> m
                                                                   (update-in [:conn] dissoc :socket)
                                                                   (assoc-in [:conn :connected?] false)))))})]
    (swap! *db
           (fn [m1 m2]
             (merge-with merge m1 m2))
           {:conn {:socket socket}})))

(defn ^{:cmd "calva.v2.disconnect"} disconnect []
  (let [^js socket (get-in @*db [:conn :socket])]
    (.end socket)))

(defn ^{:cmd "calva.v2.state"} state []
  (js/console.log "State" (clj->js @*db)))

