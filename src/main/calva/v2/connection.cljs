(ns calva.v2.connection
  (:require
   [citrus.core :as citrus]
   [kitchen-async.promise :as p]
   [calva.v2.nrepl :as nrepl]
   [calva.v2.output :as output]
   [calva.v2.gui :as gui]))

(defn- state-str [r]
  (let [connection @(citrus/subscription r [:connection])]
    (if (:connected? connection)
      (str "Connected to " (:host connection) ":" (:port connection) ".")
      "Disconnected.")))

(def initial-state
  {:connected? false})

(defmulti control (fn [event & args] event))

(defmethod control :init []
  {:state initial-state})

(defn control :connect [r [host port] state]
  (if (and host port)
    (let [^js socket (nrepl/connect {:host host
                                     :port port
                                     :on-connect #(citrus/dispatch! r :connection :on-connect)
                                     :on-end #(citrus/dispatch! r :connection :on-end)})]

      {:state (assoc state
                     :host host
                     :port port
                     :socket socket)
       :on-})
    {:state state}))

(defmethod control :on-connect [r _ state]
  (output/append-line r (state-str r))
  (assoc state :connected? true))

(defmethod control :on-end [r _ state]
  (output/append-line r (state-str r))
  (assoc state :connected? false))

(defn ^{:cmd "calva.v2.connect"} connect [r]
  (p/let [host (gui/show-input-box {:placeHolder "nREPL Server Address"
                                    :ignoreFocusOut true
                                    :value "localhost"})

          port (gui/show-input-box {:placeHolder "nREPL Server Port"
                                    :ignoreFocusOut true})

          connect #(citrus/dispatch! r :connection :connect host port)]

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

(defn ^{:cmd "calva.v2.connectThenDisconnect"} connect-then-disconnect [db]
  (-> db
      (connect)
      (disconnect)))
