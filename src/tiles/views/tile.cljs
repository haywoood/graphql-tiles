(ns tiles.views.tile
  (:require [reagent.core :as r]))

(defn tile
  [{:keys [selected? on-click background-color color handler width height]
    :or   {width    40
           height   60
           on-click identity
           handler  identity}
    :as   props}]
  [:div {:style       (merge
                       {:width           width
                        :height          height
                        :position        "relative"
                        :box-sizing      "border-box"
                        :backgroundColor background-color}
                       (when selected?
                         {:border "2px solid blue"}))
         :draggable   "false"
         :onMouseOver #(handler % [:on-mouse-over tile])
         :onMouseDown on-click
         :onMouseUp   #(handler % [:on-mouse-up tile])}
   [:div {:style {:position        "absolute"
                  :bottom          18
                  :left            19
                  :width           4
                  :height          4
                  :borderRadius    2
                  :backgroundColor color}}]])
