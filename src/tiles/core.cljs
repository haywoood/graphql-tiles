(ns tiles.core
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [tiles.store :as s]
            [tiles.views.legend :refer [legend]]
            [tiles.views.board :refer [board]]))

(defn tiles-root []
  [:div {:style {:display         "flex"
                 :flex            1
                 :flex-direction  "column"
                 :align-items     "center"
                 :justify-content "center"}}
   [:div {:style {:flex    1
                  :display "flex"}}
    [board]]
   [legend]])

(defn ^:export init []
  (rf/dispatch-sync [:initialize])
  (r/render [tiles-root]
            (.getElementById js/document "app")))

