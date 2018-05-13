(ns calva.v2.language)

(defn ClojureLanguageConfiguration []
  (this-as this
           (set! (.-wordPattern this) #"[^\[\]\(\)\{\};\s\"\\]+")
           (set! (.-indentationRules this) #js {:increaseIndentPattern #"[\[\(\{]"
                                                :decreaseIndentPattern nil})))
