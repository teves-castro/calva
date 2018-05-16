(ns calva.v2.gui
  (:require ["vscode" :as vscode]))

(defn show-information-message [message]
  (.showInformationMessage (.-window vscode) message))


(defn show-input-box [& [options]]
  (.showInputBox (.-window vscode) (clj->js options)))