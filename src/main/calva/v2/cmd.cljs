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

(defn ^{:cmd "calva.v2.connect"} connect [{:keys [output] :as db}]
  (p/let [host (gui/show-input-box {:placeHolder "nREPL Server Address"
                                    :ignoreFocusOut true
                                    :value "localhost"})

          port (gui/show-input-box {:placeHolder "nREPL Server Port"
                                    :ignoreFocusOut true})

          connect (fn [[host port]]
                    ;; TODO
                    (if (and host port)
                      (let [^js socket (nrepl/connect {:host host
                                                       :port port
                                                       :on-connect (fn []
                                                                     (db/transact! #(let [new-db (assoc-in % [:conn :connected?] true)]
                                                                                      (output/append-line output (state-str new-db))
                                                                                      (gui/show-information-message "Connected to nREPL server")

                                                                                      new-db)))
                                                       :on-end (fn []
                                                                 (db/transact! #(let [new-db (dissoc % :conn)]
                                                                                  (output/append-line output (state-str new-db))
                                                                                  (gui/show-information-message  "Disconnected from nREPL server")

                                                                                  new-db)))})]

                        (-> db
                            (assoc-in [:conn :host] host)
                            (assoc-in [:conn :port] port)
                            (assoc-in [:conn :socket] socket)))

                      ;; don't change `db`
                      db))]

    (p/-> (p/all [host port])
          (connect))))

(defn ^{:cmd "calva.v2.disconnect"} disconnect [db]
  (when-let [^js socket (get-in db [:conn :socket])]
    (.end socket))

  db)

(defn ^{:cmd "calva.v2.state"} state [db]
  (let [^js output (:output db)]
    (output/append-line-and-show output (state-str db)))

  db)

