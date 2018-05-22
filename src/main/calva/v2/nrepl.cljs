(ns calva.v2.nrepl
  (:require
   ["vscode" :as vscode]
   ["nrepl-client" :as nrepl-client]
   
   [cljs-node-io.core :as io.core]
   [cljs-node-io.fs :as io.fs])
  
  (:refer-clojure :exclude [clone]))

(defn slurp-port []
  (let [path (str (-> vscode .-workspace .-rootPath) "/.nrepl-port")]
    (when (io.fs/file? path)
      (io.core/slurp path))))

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

