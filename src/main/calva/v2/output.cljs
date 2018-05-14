(ns calva.v2.output)


(defn append-line
  "Append the given value and a line feed character to the channel."
   [^js output value]
  (.appendLine output value))


(defn append-line-and-show
  "Append the given value and a line feed character to the channel and reveals it."
   [^js output value]
  (.appendLine output value)
  (.show output false))
