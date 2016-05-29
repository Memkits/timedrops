
(ns timedrops.updater.core
  (:require [timedrops.updater.time :as time]
            [timedrops.updater.event :as event]))

(defn updater [db op op-data op-id op-time]
  (case
    op
    :time/tick
    (time/tick db op-data op-id op-time)
    :event/add
    (event/add db op-data op-id op-time)
    db))
