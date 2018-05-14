(ns calva.v2.cmd
  (:require

   [calva.v2.db :as db]
   [calva.repl.nrepl :as nrepl]
   [calva.v2.output :as output]))

(defn- state-str [db]
  (if (get-in db [:conn :connected?])
    (str "Connected to " (get-in db [:conn :host]) ":" (get-in db [:conn :port]) ".")
    "Disconnected."))

(defn ^{:cmd "calva.v2.connect"} connect [{:keys [output] :as db}]
  (let [host "localhost"
        port 52165
        ^js socket (nrepl/connect {:host host
                                   :port port
                                   :on-connect (fn []
                                                 (let [new-db (db/mutate! #(assoc-in % [:conn :connected?] true))]
                                                   (output/append-line-and-show output (state-str new-db))))
                                   :on-end (fn []
                                             (let [new-db (db/mutate! #(dissoc % :conn))]
                                               (output/append-line-and-show output (state-str new-db))))})]

    (-> db
        (assoc-in [:conn :host] host)
        (assoc-in [:conn :port] port)
        (assoc-in [:conn :socket] socket))))

(defn ^{:cmd "calva.v2.disconnect"} disconnect [db]
  (when-let [^js socket (get-in db [:conn :socket])]
    (.end socket))

  db)

(defn ^{:cmd "calva.v2.state"} state [db]
  (let [^js output (:output db)]
    (output/append-line-and-show output (state-str db)))

  db)

(defn ^{:cmd "calva.v2.connectThenDisconnect"} connect-then-disconnect [db]
  (-> db
      (connect)
      (disconnect)))
