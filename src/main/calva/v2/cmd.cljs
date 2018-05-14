(ns calva.v2.cmd
  (:require
   [cljs.pprint :as pprint]

   [calva.v2.db :as db]
   [calva.repl.nrepl :as nrepl]
   [calva.v2.output :as output]))

(defn- state-str [*db]
  (if (get-in @*db [:conn :connected?])
    (str "Connected to " (get-in @*db [:conn :host]) ":" (get-in @*db [:conn :port]) ".")
    "Disconnected."))


(defn ^{:cmd "calva.v2.connect"} connect [*db]
  (let [^js output (:output @*db)
        
        ^js socket (nrepl/connect (let [host "localhost"
                                        port 50656]
                                    {:host host
                                     :port port
                                     :on-connect (fn []
                                                   (db/mutate! *db #(-> %
                                                                        (assoc-in [:conn :host] host)
                                                                        (assoc-in [:conn :port] port)
                                                                        (assoc-in [:conn :connected?] true)))

                                                   (output/append-line-and-show output (state-str *db)))
                                     :on-end (fn []
                                               (db/mutate! *db #(dissoc % :conn))
                                               
                                               (output/append-line-and-show output (state-str *db)) )}))]
    (db/mutate! *db #(assoc-in % [:conn :socket] socket))))

(defn ^{:cmd "calva.v2.disconnect"} disconnect [*db]
  (when-let [^js socket (get-in @*db [:conn :socket])]
    (.end socket)))

(defn ^{:cmd "calva.v2.state"} state [*db]
  (let [^js output (:output @*db)]
    (output/append-line-and-show output (state-str *db))))
