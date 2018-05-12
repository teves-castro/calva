(ns calva.v2.cmd)


(defn ^{:cmd "calva.v2.connect"} connect []
  (js/console.log "Connect"))


(defn ^{:cmd "calva.v2.disconnect"} disconnect []
  (js/console.log "Disconnect"))


(defn ^{:cmd "calva.v2.state"} state []
  (js/console.log "State"))

