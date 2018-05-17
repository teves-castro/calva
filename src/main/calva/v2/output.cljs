(ns calva.v2.output
  (:require
   ["vscode" :as vscode]
   [citrus.core :as citrus]))

(def initial-state
  nil)

(defmulti control (fn [event & args] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :create-output-channel [_ [name greeting] _]
  (let [^js output-channel (-> (.-window vscode)
                               (.createOutputChannel name))]
    {:state output-channel
     :output-line greeting}))

(defmethod control :output-line [_ [line show] state]
  {:output-line {:output state
                 :line line
                 :show show}})

(defn output-line
  [r cn {:keys [line show ^js output] :as effect}]
  (js/console.log "output-line: " line show))
  (when output
    (.appendLine output line)
    (when show
      (.show output false))))

(defn append-line
  "Append the given value and a line feed character to the channel."
  [r value]
  (citrus/dispatch! r :output :output-line value false))

(defn append-line-and-show
  "Append the given value and a line feed character to the channel and reveals it."
  [r value]
  (citrus/dispatch! r :output :output-line value true))