(ns calva.extension
  (:require
   ["vscode" :as vscode]

   [calva.v2.language :as language]
   [calva.v2.db :as db]
   [calva.v2.output :as output]
   [calva.v2.cmd :as cmd]))

(defn- register-command [context sys cmd]
  (-> (.-subscriptions context)
      (.push (-> vscode
                 (.-commands)
                 (.registerCommand (-> cmd meta :cmd) #(cmd sys))))))

(defn activate [^js context]
  (-> (.-languages vscode)
      (.setLanguageConfiguration "clojure" (language/ClojureLanguageConfiguration.)))

  (let [^js output (-> (.-window vscode)
                       (.createOutputChannel "Calva"))

        sys        {:*db    (atom {})
                    :output output}]

    (register-command context sys #'cmd/connect)
    (register-command context sys #'cmd/disconnect)
    (register-command context sys #'cmd/state)

    (output/append-line output "Calva is active.")))

(defn exports []
  #js {:activate activate})
