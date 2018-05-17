(ns calva.extension
  (:require
   ["vscode" :as vscode]
   [calva.v2.language :as language]
   [calva.v2.connection :as connection]
   [citrus.core :as citrus]
   [calva.v2.state :as state]
   [calva.v2.output :as output]))

(defn- register-command [r context cmd]
  (-> (.-subscriptions context)
      (.push (-> vscode
                 (.-commands)
                 (.registerCommand (-> cmd meta :cmd) #(cmd r))))))

(defn activate [^js context]
  (let [reconciler state/reconciler]
    (citrus/dispatch-sync! reconciler :output :create-output-channel "Calva says")
    (output/append-line reconciler "Calva activated  ❤️")
    (-> (.-languages vscode)
        (.setLanguageConfiguration "clojure" (language/ClojureLanguageConfiguration.)))

    (register-command reconciler context #'connection/connect-cmd)
    (register-command reconciler context #'connection/disconnect)
    (register-command reconciler context #'connection/connect-then-disconnect)
    (register-command reconciler context #'connection/state)))

(defn exports []
  #js {:activate activate})
