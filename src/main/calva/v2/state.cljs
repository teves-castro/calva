(ns calva.v2.state
  (:require [cljs.spec.alpha :as s]
            [citrus.core :as citrus]))

(def initial-state
  {:conn {:connected? false}})

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

(defonce reconciler
  (citrus/reconciler
    {:state
     (atom {})
     :controllers
     {:calva control}

(defonce init-controllers (citrus/broadcast-sync! reconciler :init))
