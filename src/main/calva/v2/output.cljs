(ns calva.v2.output
  (:require [calva.v2.db :refer [*db]]))


(defn append-line
  "Append the given value and a line feed character to the channel."
   [value]
  (let [^js output (:output @*db)]
    (.appendLine output value)))