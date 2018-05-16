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

(defn ^{:cmd "calva.v2.connect"} connect []
  (p/let [host (gui/show-input-box {:placeHolder "nREPL Server Address"
                                    :ignoreFocusOut true
                                    :value "localhost"})

          port (gui/show-input-box {:placeHolder "nREPL Server Port"
                                    :ignoreFocusOut true})]

    (p/then (p/all [host port])
            (fn [[host port]]
              ;; TODO
              (when (and host port)
                (let [^js socket (nrepl/connect {:host host
                                                 :port port
                                                 :on-connect (fn []
                                                               (db/mutate! (fn [{:keys [output] :as db}]
                                                                             (let [new-db (assoc-in db [:conn :connected?] true)]
                                                                               (output/append-line output (state-str new-db))
                                                                               (gui/show-information-message "Connected to nREPL server")

                                                                               new-db))))
                                                 :on-end (fn []
                                                           (db/mutate! (fn [{:keys [output] :as db}]
                                                                         (let [new-db (dissoc db :conn)]
                                                                           (output/append-line output (state-str new-db))
                                                                           (gui/show-information-message  "Disconnected from nREPL server")

                                                                           new-db))))})]

                  (db/mutate! (fn [db]
                                (-> db
                                    (assoc-in [:conn :host] host)
                                    (assoc-in [:conn :port] port)
                                    (assoc-in [:conn :socket] socket))))))))))

(defn ^{:cmd "calva.v2.disconnect"} disconnect []
  (db/mutate! (fn [db]
                (when-let [^js socket (get-in db [:conn :socket])]
                  (.end socket))

                db)))

(defn ^{:cmd "calva.v2.state"} state []
  (db/mutate! (fn [db]
                (let [^js output (:output db)]
                  (output/append-line-and-show output (state-str db)))

                db)))

