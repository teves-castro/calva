(ns calva.extension
  (:require
   ["vscode" :as vscode]
   [citrus.core :as citrus]
   [calva.v2.language :as language]
   [calva.v2.connection :as connection]
   [calva.v2.state :as state]))

(defn- register-command [r context cmd]
  (-> (.-subscriptions context)
      (.push (-> vscode
                 (.-commands)
                 (.registerCommand (-> cmd meta :cmd) #(cmd r))))))

(defn activate [^js context]
  (let [reconciler state/reconciler]
    (citrus/dispatch! reconciler :output :create-output-channel "Calva says"  "Calva activated  ❤️")
    (-> (.-languages vscode)
        (.setLanguageConfiguration "clojure" (language/ClojureLanguageConfiguration.)))

    (register-command reconciler context #'connection/connect-cmd)
    (register-command reconciler context #'connection/disconnect-cmd)
    (register-command reconciler context #'connection/connect-then-disconnect)
    (register-command reconciler context #'connection/state)))

(defn exports []
  #js {:activate activate})
