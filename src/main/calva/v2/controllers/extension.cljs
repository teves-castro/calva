(ns calva.vs.controllers.connection)

(def initial-state
  {:connected? false})

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})
