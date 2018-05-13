(ns calva.v2.cmd
  (:require [calva.v2.db :refer [*db]]
            [calva.repl.nrepl :as nrepl]))

(defn ^{:cmd "calva.v2.connect"} connect []
  (let [^js socket (nrepl/connect {:host "localhost"
                                   :port "64772"
                                   :on-connect #(swap! *db (fn [db]
                                                             (assoc-in db [:conn :connected?] true)))
                                   :on-end #(swap! *db (fn [db]
                                                         (let [^js socket (get-in db [:conn :socket])]
                                                           (-> db
                                                               (update-in [:conn] dissoc :socket)
                                                               (assoc-in [:conn :connected?] false)))))})]
    (swap! *db (fn [db]
                 (assoc-in db [:conn :socket] socket)))))

(defn ^{:cmd "calva.v2.disconnect"} disconnect []
  (let [^js socket (get-in @*db [:conn :socket])]
    (.end socket)))

(defn ^{:cmd "calva.v2.state"} state []
  (js/console.log "State" (clj->js @*db)))

