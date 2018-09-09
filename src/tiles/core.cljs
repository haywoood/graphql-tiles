(ns tiles.core
  (:require [re-frame.core :as rf]
            [tiles.store :as s]
            [reagent.core :as r]))

(defn tiles-root []
  [:h1 "sup ld"])

(defn ^:export init []
  (rf/dispatch-sync [:initialize])
  (r/render [tiles-root]
            (.getElementById js/document "app")))

