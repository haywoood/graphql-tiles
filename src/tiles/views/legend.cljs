(ns tiles.views.legend
  (:require [artemis.document :as d]
            [re-frame.core :as rf]
            [tiles.views.tile :refer [tile]]))

(defn handle-select
  "Update the selected tile in the store"
  [legend-slug tile-slug]
  (rf/dispatch [:write
                {:legend
                 {:slug legend-slug
                  :selected { :slug tile-slug }}}
                (d/parse-document "{legend { slug selected { slug }}}")]))

(def legend-query
  (d/parse-document
   "{
      legend {
        slug
        selected { slug }
        tiles {
          slug
          color
          backgroundColor
        }
      }
    }"))

(defn legend []
  (let [{:keys [legend]} @(rf/subscribe [:read legend-query])]
    [:div {:style {:display "flex"
                   :margin-bottom 5}}
     (for [{:keys [color backgroundColor slug] :as tile*} (-> legend :tiles)]
       ^{:key slug}
       [tile {:selected?        (= slug (-> legend :selected :slug))
              :on-click         #(handle-select (:slug legend) slug)
              :color            color
              :background-color backgroundColor}])]))
