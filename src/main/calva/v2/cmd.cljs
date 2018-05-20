(ns calva.v2.cmd
  (:require
   ["vscode" :as vscode]

   [kitchen-async.promise :as p]

   [calva.v2.db :as db]
   [calva.v2.nrepl :as nrepl]
   [calva.v2.output :as output]
   [calva.v2.gui :as gui]))

(defn- state-str [db]
  (if (get-in db [:conn :connected?])
    (str "Connected to " (get-in db [:conn :host]) ":" (get-in db [:conn :port]) ".")
    "Disconnected."))

(defn nrepl-connected [{:keys [*db output]}]
  (db/mutate! *db (fn [db]
                    (assoc-in db [:conn :connected?] true)))

  (output/append-line output (state-str @*db))
  (gui/show-information-message "Connected to nREPL server"))

(defn nrepl-disconnected [{:keys [*db output]}]
  (db/mutate! *db (fn [db]
                    (dissoc db :conn)))

  (output/append-line output (state-str @*db))
  (gui/show-information-message "Disconnected from nREPL server"))

(defn nrepl-try-to-connect [{:keys [*db] :as sys} host port]
  (when (and host port)
    (let [^js socket (nrepl/connect {:host       host
                                     :port       port
                                     :on-connect #(nrepl-connected sys)
                                     :on-end     #(nrepl-disconnected sys)})]

      (db/mutate! *db (fn [db]
                        (-> db
                            (assoc-in [:conn :host] host)
                            (assoc-in [:conn :port] port)
                            (assoc-in [:conn :socket] socket)))))))

(defn ^{:cmd "calva.v2.connect"} connect [{:keys [*db output] :as sys}]
  (p/let [host (gui/show-input-box {:placeHolder    "nREPL Server Address"
                                    :ignoreFocusOut true
                                    :value          "localhost"})

          port (gui/show-input-box {:placeHolder    "nREPL Server Port"
                                    :ignoreFocusOut true})]

    (nrepl-try-to-connect sys host port)))

(defn ^{:cmd "calva.v2.disconnect"} disconnect [{:keys [*db]}]
  (when-let [^js socket (get-in @*db [:conn :socket])]
    (.end socket)))

(defn ^{:cmd "calva.v2.state"} state [{:keys [*db output]}]
  (output/append-line-and-show output (state-str @*db)))

