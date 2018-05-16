(ns calva.v2.state
  (:require [citrus.core :as citrus]))

(defonce reconciler
  (citrus/reconciler
   :batched-updates
   {:schedule-fn js/setImmediate :release-fn js/clearImmediate}
   :state
   (atom {})
   :controllers
   {:connection connection-control
    :extension extension-control}))

(defonce init-controllers (citrus/broadcast-sync! reconciler :init))
