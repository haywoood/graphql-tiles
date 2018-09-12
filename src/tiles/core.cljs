(ns tiles.core
  (:require [keybind.core :as k]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [tiles.store :as s]
            [tiles.views.board :refer [board]]
            [tiles.views.legend :refer [legend]]
            [tiles.views.sidebar :refer [sidebar]]))

(defn tiles-root
  "Main component"
  []
  [:div {:style {:display         "flex"
                 :flex            1
                 :flex-direction  "column"
                 :align-items     "center"
                 :justify-content "center"}}
   [sidebar]
   [:div {:style {:flex 1 :display "flex"}}
    [board]]
   [legend]])

(defn ^:export init []
  (rf/dispatch-sync [:initialize])

  ;; set up global undo/redo keybindings
  ;; defmod is cmd for macs and ctrl everywhere else
  (k/bind! "defmod-z"       :global-undo #(rf/dispatch [:undo]))
  (k/bind! "defmod-shift-z" :global-redo #(rf/dispatch [:redo]))

  ;; Mount the app into the DOM
  (r/render [tiles-root] (.getElementById js/document "app")))

