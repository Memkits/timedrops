
(ns timedrops.component.container
  (:require [hsl.core :refer [hsl]]
            [respo.alias :refer [create-comp div span button]]
            [timedrops.component.portal :refer [comp-portal]]))

(defn render [store] (fn [state mutate] (div {} (comp-portal store))))

(def comp-container (create-comp :container render))
