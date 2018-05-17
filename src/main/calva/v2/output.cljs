(ns calva.v2.output
  (:require
   ["vscode" :as vscode]
   [citrus.core :as citrus]))

(def initial-state
  nil)

(defmulti control (fn [event & args] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :create-output-channel [_ [name] _]
  (let [^js output-channel (-> (.-window vscode)
                               (.createOutputChannel name))]
    {:state output-channel}))

(defn append-line
  "Append the given value and a line feed character to the channel."
  [r value]
  (let [^js output @(citrus/subscription r [:output])]
    (when output
      (.appendLine output value))))

(defn append-line-and-show
  "Append the given value and a line feed character to the channel and reveals it."
  [r value]
  (let [^js output @(citrus/subscription r [:output])]
    (when output
      (.appendLine output value)
      (.show output false))))
