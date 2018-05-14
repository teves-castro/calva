(ns calva.v2.cmd
  (:require 
   [calva.v2.db :as db]
   [calva.repl.nrepl :as nrepl]))

(defn ^{:cmd "calva.v2.connect"} connect [*db]
  (let [^js socket (nrepl/connect {:host "localhost"
                                   :port "50656"
                                   :on-connect (fn []
                                                 (db/mutate! *db #(assoc-in % [:conn :connected?] true))
                                                 
                                                 (js/console.log "nrepl connected"))
                                   :on-end (fn [] 
                                             (db/mutate! *db #(-> %
                                                                  (update-in [:conn] dissoc :socket)
                                                                  (assoc-in [:conn :connected?] false))))})]
    (db/mutate! *db #(assoc-in % [:conn :socket] socket))))

(defn ^{:cmd "calva.v2.disconnect"} disconnect [*db]
  (let [^js socket (get-in @*db [:conn :socket])]
    (.end socket)))

(defn ^{:cmd "calva.v2.state"} state [*db]
  (js/console.log "State" (clj->js @*db)))

