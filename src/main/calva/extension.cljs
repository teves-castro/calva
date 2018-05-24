(ns calva.extension
  (:require
   ["vscode" :as vscode]

   [calva.v2.language :as language]
   [calva.v2.db :as db]
   [calva.v2.output :as output]
   [calva.v2.cmd :as cmd]
   [calva.v2.nrepl :as nrepl]))

(def *db
  (atom {}))


(def *sys
  (atom {}))


(defn register-disposable [^js context ^js disposable]
  (.push context.subscriptions disposable))


(defn- register-command [sys cmd]
  (vscode/commands.registerCommand (-> cmd meta :cmd) #(cmd sys)))


(defn setup-cmd [^js context sys cmd]
  (->> (register-command sys cmd)
       (register-disposable context)))


(defn activate [^js context]
  (vscode/languages.setLanguageConfiguration "clojure" (language/ClojureLanguageConfiguration.))
  
  (let [^js output (vscode/window.createOutputChannel "Calva")]

    (reset! *sys {:*db    *db
                  :output output})
    
    (register-disposable context (vscode/Disposable.from output))
    
    (setup-cmd context @*sys #'cmd/connect)
    (setup-cmd context @*sys #'cmd/disconnect)
    (setup-cmd context @*sys #'cmd/state)

    (output/append-line output "Calva is active.")))


(defn deactivate []
  (nrepl/disconnect (get @*db :conn)))


(defn exports []
  #js {:activate   activate
       :deactivate deactivate})
