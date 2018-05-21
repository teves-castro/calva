(ns calva.v2.cmd
  (:require
   ["vscode" :as vscode]

   [kitchen-async.promise :as p]

   [calva.v2.db :as db]
   [calva.v2.nrepl :as nrepl]
   [calva.v2.output :as output]
   [calva.v2.gui :as gui]))


(defn connected-str [{:keys [conn]}]
  (str "Connected - nrepl://" (:host conn) ":" (:port conn)))


(defn disconnected-str [{:keys [conn]}]
  (str "Disconnected - nrepl://" (:host conn) ":" (:port conn)))


(defn- state-str [db]
  (if (get-in db [:conn :connected?])
    (connected-str db)
    (disconnected-str db)))

(defn nrepl-connected [{:keys [*db output]}]
  (db/mutate! *db (fn [db]
                    (assoc-in db [:conn :connected?] true)))

  (output/append-line output (state-str @*db))

  (gui/show-information-message (connected-str @*db))

  (-> (get-in @*db [:conn :socket])
      (.clone (fn [err result]
                (when-not err
                  (let [[{:keys [new-session]}] (js->clj result :keywordize-keys true)]
                    (output/append-line output "Clojure session initialized.")

                    (db/mutate! *db #(assoc-in % [:conn :clj-session] new-session))))))))

(defn nrepl-disconnected [{:keys [*db output]}]
  (db/mutate! *db #(assoc-in % [:conn :connected?] false))

  (output/append-line output (disconnected-str @*db))

  (gui/show-information-message (disconnected-str @*db)))

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

