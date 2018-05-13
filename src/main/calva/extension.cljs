(ns calva.extension
  (:require 
    ["vscode" :as vscode]
    ["/calva/language" :default ClojureLanguageConfiguration]
            
    [calva.v2.db :as db]
    [calva.v2.output :as output]
    [calva.v2.cmd :as cmd]))


(defn- register-command [context cmd]
  (-> (.-subscriptions context)
      (.push (-> vscode
                 (.-commands)
                 (.registerCommand (-> cmd meta :cmd) cmd)))))


(defn activate [^js context]
  (-> (.-languages vscode)
      (.setLanguageConfiguration "clojure" (ClojureLanguageConfiguration.)))
  
  (db/mutate! (fn [db]
                (assoc db :output (-> (.-window vscode)
                                      (.createOutputChannel "Calva")))))
  
  (register-command context #'cmd/connect)
  (register-command context #'cmd/disconnect)
  (register-command context #'cmd/state)
  
  (output/append-line "Calva is active."))


(defn exports []
  #js {:activate activate})
