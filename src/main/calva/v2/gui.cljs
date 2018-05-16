(ns calva.v2.gui
  (:require ["vscode" :as vscode]))

(defn show-information-message [message]
  (.showInformationMessage (.-window vscode) message))