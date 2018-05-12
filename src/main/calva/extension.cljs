(ns calva.extension
  (:require 
    ["vscode" :as vscode]
    ["/calva/language" :default ClojureLanguageConfiguration]
            
    [calva.v2.cmd :as cmd]))


(defn- register-command [context cmd]
  (-> (.-subscriptions context)
      (.push (-> vscode
                 (.-commands)
                 (.registerCommand (-> cmd meta :cmd) cmd)))))


(defn activate [^js context]
  (js/console.log "Calva is active.")
  
  (-> (.-languages vscode)
      (.setLanguageConfiguration "clojure" (ClojureLanguageConfiguration.)))
  
  (register-command context #'cmd/connect)
  (register-command context #'cmd/disconnect)
  (register-command context #'cmd/state))


(defn exports []
  #js {:activate activate})
