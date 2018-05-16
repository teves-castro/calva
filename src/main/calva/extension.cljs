(ns calva.extension
  (:require
   ["vscode" :as vscode]

   [kitchen-async.promise :as p]

   [calva.v2.language :as language]
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
      (.setLanguageConfiguration "clojure" (language/ClojureLanguageConfiguration.)))

  (register-command context #'cmd/connect)
  (register-command context #'cmd/disconnect)
  (register-command context #'cmd/state)

  ;; Initialize db
  (db/mutate! (fn [db]
                (let [^js output (-> (.-window vscode)
                                     (.createOutputChannel "Calva"))

                      new-db (assoc db :output output)]

                  (output/append-line output "Calva is active.")

                  new-db))))

(defn exports []
  #js {:activate activate})
