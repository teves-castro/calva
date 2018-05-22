(ns calva.v2.nrepl
  (:require
   ["nrepl-client" :as nrepl-client])
  (:refer-clojure :exclude [clone]))

(defn connect
  "Connects to a socket-based REPL at the given host (defaults to localhost) and port."
  [options]
  (let [{:keys [host port on-connect on-error on-end] :or {host "localhost"}} options]
    (doto (.connect nrepl-client #js {:host host :port port})
      (.once "connect" (fn []
                         (when on-connect
                           (on-connect))))

      (.once "end" (fn []
                     (when on-end
                       (on-end))))

      (.on "error" (fn [error]
                     (when on-error
                       (on-error error)))))))

(defn clone
  ([client callback]
    (.clone client callback))
  
  ([client session callback]
    (.clone client session callback)))

