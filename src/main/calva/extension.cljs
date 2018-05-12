(ns calva.extension
  (:require 
    ["vscode" :as vscode]
    ["/calva/language" :default ClojureLanguageConfiguration]))


(defn cmd-connect []
  (js/console.log "Connect"))


(defn cmd-disconnect []
  (js/console.log "Disconnect"))


(defn cmd-state []
  (js/console.log "State"))


(def cmds
  {"calva.v2.connect" cmd-connect
   "calva.v2.disconnect" cmd-disconnect
   "calva.v2.state" cmd-state})


(defn activate [^js context]
  (js/console.log "Calva is active.")
  
  (-> (.-languages vscode)
      (.setLanguageConfiguration "clojure" (ClojureLanguageConfiguration.)))
  
  (doseq [[k f] cmds]
    (-> (.-subscriptions context)
        (.push (-> vscode
                   (.-commands)
                   (.registerCommand k f))))))


(defn exports []
  #js {:activate activate})
