(ns calva.v2.connection
  (:require
   [citrus.core :as citrus]
   [kitchen-async.promise :as p]
   [calva.v2.nrepl :as nrepl]
   [calva.v2.output :as output]
   [calva.v2.gui :as gui]))

(def initial-state
  {:connected? false})

(defmulti control (fn [event & args] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :on-connect [r _ state]
  (js/console.log "on-connect")
  {:state (assoc state :connected? true)
   :output-line {:line (pr-str state)}})

(defmethod control :on-end [r _ state]
  (js/console.log "on-end")
  #_(output/append-line r (pr-str state))
  {:state {:connected? false}
   :output-line {:line (pr-str state)}})

(defmethod control :connect [r [host port] state]
  (js/console.log "connect: " host port)
  {:state
   (assoc state
          :host host
          :port port)
   :nrepl-connect
   {:host host
    :port port
    :on-connect :on-connect
    :on-end :on-end
    :got-socket :got-socket}
   :output-line
   {:line (pr-str state)}})

(defmethod control :got-socket [r [^js socket] state]
  (js/console.log "got socket: " socket)
  {:state
   (assoc state
          :socket socket)})

(defn nrepl-connect [r cn {:keys [host port on-connect on-end got-socket] :as effect}]
  (js/console.log "nrepl-connect: " host port)
  (when (and host port)
    (let [^js socket (nrepl/connect {:host host
                                     :port port
                                     :on-connect #(citrus/dispatch! r cn on-connect)
                                     :on-end #(citrus/dispatch! r cn on-end)})]
      (citrus/dispatch! r cn got-socket socket))))

(defn ^{:cmd "calva.v2.connect"} connect-cmd [r]
  (p/let [host (gui/show-input-box {:placeHolder "nREPL Server Address"
                                    :ignoreFocusOut true
                                    :value "localhost"})

          port (gui/show-input-box {:placeHolder "nREPL Server Port"
                                    :ignoreFocusOut true})]
    (p/->> (p/all [host port])
      (concat)
      (apply citrus/dispatch! r :connection :connect))))

(defn ^{:cmd "calva.v2.disconnect"} disconnect [r]
  (when-let [^js socket @(citrus/subscription r [:connection :socket])]
    (.end socket)))

(defn ^{:cmd "calva.v2.state"} state [r]
  (citrus/dispatch! r :output :output-line {:line (pr-str state)
                                            :show true}))

(defn ^{:cmd "calva.v2.connectThenDisconnect"} connect-then-disconnect [r]
  (-> r
      (connect-cmd)
      (disconnect)))
