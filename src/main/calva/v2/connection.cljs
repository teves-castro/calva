(ns calva.v2.connection
  (:require
   [citrus.core :as citrus]
   [kitchen-async.promise :as p]
   [calva.v2.nrepl :as nrepl]
   [calva.v2.gui :as gui]))

(def initial-state
  {:connected? false
   :connecting? false})

(defmulti control (fn [event & args] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :on-connect [r _ state]
  (let [new-state (assoc state
                         :connected? true
                         :connecting? false)]
    {:state new-state
     :output-line {:line (str "on-connect: " (pr-str new-state))}}))

(defmethod control :on-end [r _ state]
  (let [new-state {:connected? false}]
    {:state new-state
     :output-line {:line (str "on-end: " (pr-str new-state))}}))

(defmethod control :save-socket [r [^js socket] state]
  (let [new-state (assoc state :socket socket)]
    {:state
     new-state
     :output-line {:line (str "save-socket: " (pr-str new-state))}}))

(defmethod control :save-host-and-port [r [host port] state]
  (let [new-state (assoc state
                         :host host
                         :port port)]
    {:state
     new-state
     :output-line
     {:line (str "save-host-and-port: " (pr-str state))}}))

(defmethod control :connect [r _ state]
  (let [new-state (assoc state
                         :connecting? true)]
    {:state
     new-state
     :nrepl-connect
     {:on-connect :on-connect
      :on-end :on-end
      :save-socket :save-socket}
     :output-line
     {:line
      (str "connect: " (pr-str new-state))}}))

(defmethod control :disconnect [r _ state]
  {:socket-disconnect
   {:socket (:socket state)}
   :output-line
   {:line (str "disconnect: " (pr-str state))}})

(defn nrepl-connect [r cn {:keys [on-connect on-end save-socket]}]
  (p/let [host (gui/show-input-box {:placeHolder "nREPL Server Address"
                                    :ignoreFocusOut true
                                    :value "localhost"})

          port (gui/show-input-box {:placeHolder "nREPL Server Port"
                                    :ignoreFocusOut true})]
    (when (and host port)
      (citrus/dispatch! r cn :save-host-and-port host port)
      (->> (nrepl/connect {:host host
                           :port port
                           :on-connect #(citrus/dispatch! r cn on-connect)
                           :on-end #(citrus/dispatch! r cn on-end)})
           (citrus/dispatch! r cn save-socket)))))

(defn socket-disconnect [r cn {:keys [socket]}]
  (when socket
    (.end socket)))

(defn ^{:cmd "calva.v2.connect"} connect-cmd [r]
  (citrus/dispatch! r :connection :connect))

(defn ^{:cmd "calva.v2.disconnect"} disconnect-cmd [r]
  (citrus/dispatch! r :connection :disconnect))

(defn ^{:cmd "calva.v2.state"} state [r]
  (citrus/dispatch! r :output :output-line (str "connection state: " (pr-str @(citrus/subscription r [:connection])))))

;;; This is not the way to do this. Obviously
(defn ^{:cmd "calva.v2.connectThenDisconnect"} connect-then-disconnect [r]
  (citrus/dispatch-sync! r :connection :connect)
  (citrus/dispatch-sync! r :connection :disconnect))
