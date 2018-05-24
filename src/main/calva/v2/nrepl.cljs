(ns calva.v2.nrepl
  (:require
   ["vscode" :as vscode]
   ["nrepl-client" :as nrepl-client]
   
   [kitchen-async.promise :as p]
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
  "Clones the current session, returning the ID of the newly-created session.
  
  Optional parameters
    `session` The ID of the session to be cloned; if not provided, 
              a new session with default bindings is created, 
              and mapped to the returned session ID."
  ([client callback]
    (.clone client callback))
  
  ([client session callback]
    (.clone client session callback)))

(defn close 
  "Closes the specified session.
  
  Required parameters
  `session` The ID of the session to be closed."
  [client session callback]
  (.close client session callback))


(defn disconnect 
  "`disconnect` is not part of the nREPL API, but it's a handy
  function we can use to close the Clojure and ClojureScript
  sessions and then disconnect.

  *It's fine to call this function without a
  Clojure or ClojureScript session."
  [{:keys [socket clj-session cljs-session]}]
  ;; TODO
  ;; Close sessions before disconnecting
  ;; 1. Close ClojureScript session if there is one
  ;; 2. Close Clojure session - this one is created as soon as Calva connects
  ;;
  ;; *This should also be done whenever Calva is disposed

  (when socket
    (p/do
     ;; close clj-session
     (when clj-session
       (p/promise [resolve reject]
          (close socket clj-session (fn [err result]
                                      (if err 
                                        (reject err)
                                        (resolve result))))))
     ;; once the sessions were closed
     ;; it's fine to disconnect
     (.end socket))))
