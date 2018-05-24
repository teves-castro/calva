(ns calva.extension
  (:require
   ["vscode" :as vscode]

   [cljfmt.core :as cljfmt]
   
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


(deftype ClojureDocumentRangeFormattingEditProvider []
  Object
  (provideDocumentRangeFormattingEdits [_ document range options token]
    (let [pretty (cljfmt/reformat-string (.getText document range) {:remove-consecutive-blank-lines? false})
          edit (vscode/TextEdit.replace range pretty)]
      #js [edit])))


(defn activate [^js context]
  (vscode/languages.setLanguageConfiguration "clojure" (language/ClojureLanguageConfiguration.))
  
  (let [^js output (vscode/window.createOutputChannel "Calva")]

    (reset! *sys {:*db    *db
                  :output output})
    
    (register-disposable context (vscode/Disposable.from output))
    
    
    (let [scheme   #js {:language "clojure" 
                        :scheme   "file"}
          provider (ClojureDocumentRangeFormattingEditProvider.)]
     (register-disposable context (vscode/languages.registerDocumentRangeFormattingEditProvider scheme provider)))
    
    
    (setup-cmd context @*sys #'cmd/connect)
    (setup-cmd context @*sys #'cmd/disconnect)
    (setup-cmd context @*sys #'cmd/state)

    (output/append-line output "Happy Clojure & ClojureScript coding! ❤️ \n")))


(defn deactivate []
  (nrepl/disconnect (get @*db :conn)))


(defn exports []
  #js {:activate   activate
       :deactivate deactivate})
