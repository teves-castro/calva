(ns calva.v2.cmd
  (:require
   ["vscode" :as vscode]

   [clojure.string :as str]
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
  (let [connection (if (get-in db [:conn :connected?])
                     (connected-str db)
                     (disconnected-str db))
        
        clj-session (when-let [clj-session (get-in db [:conn :clj-session])]
                      (str "Clojure session: " clj-session))]
    
    (str/join "\n" [connection clj-session])))

(defn nrepl-connected [{:keys [*db output]}]
  (db/mutate! *db (fn [db]
                    (assoc-in db [:conn :connected?] true)))

  (gui/show-information-message (connected-str @*db))

  (-> (get-in @*db [:conn :socket])
      (nrepl/clone (fn [err result]
                     (when-not err
                       (let [[{:keys [new-session]}] (js->clj result :keywordize-keys true)]
                         (db/mutate! *db #(assoc-in % [:conn :clj-session] new-session))
                         
                         (output/append-line output (state-str @*db))))))))

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
  (p/let [host (gui/show-input-box {:ignoreFocusOut true
                                    :prompt         "nREPL Host"
                                    :value          "localhost"})

          port (gui/show-input-box {:ignoreFocusOut true
                                    :prompt         "nREPL Port"
                                    :value          (nrepl/slurp-port)})]

    (nrepl-try-to-connect sys host port)))

(defn ^{:cmd "calva.v2.disconnect"} disconnect [{:keys [*db output]}]
  (nrepl/disconnect (get @*db :conn)))

(defn ^{:cmd "calva.v2.state"} state [{:keys [*db output]}]
  (output/append-line-and-show output (state-str @*db)))

