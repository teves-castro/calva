(ns calva.v2.state
  (:require [citrus.core :as citrus]
            [calva.v2.output :as output]
            [calva.v2.connection :as connection]))

(defonce reconciler
  (citrus/reconciler
   {:state
    (atom {})
    :controllers
    {:output output/control
     :connection connection/control}
    :effect-handlers
    {:output-line output/output-line
     :nrepl-connect connection/nrepl-connect}}))

(defonce init-controllers (citrus/broadcast-sync! reconciler :init))
