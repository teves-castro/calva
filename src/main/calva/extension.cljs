(ns calva.extension
  (:require
   ["vscode" :as vscode]
   
   [calva.v2.language :as language]
   [calva.v2.db :as db]
   [calva.v2.output :as output]
   [calva.v2.cmd :as cmd]))

(defn- register-command [context *db cmd]
   (-> (.-subscriptions context)
       (.push (-> vscode
                  (.-commands)
                  (.registerCommand (-> cmd meta :cmd) #(cmd *db))))))

(defn activate [^js context]
  (-> (.-languages vscode)
      (.setLanguageConfiguration "clojure" (language/ClojureLanguageConfiguration.)))

  (db/mutate! db/*db (fn [db]
                       (assoc db :output (-> (.-window vscode)
                                             (.createOutputChannel "Calva")))))
  
  (let [register-command (partial register-command context db/*db)]
    (register-command #'cmd/connect)
    (register-command #'cmd/disconnect)
    (register-command #'cmd/state))

  (output/append-line (:output @db/*db) "Calva is active."))

(defn exports []
  #js {:activate activate})
