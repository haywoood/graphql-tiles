(ns tiles.views.board
  (:require [artemis.document :as d]
            [re-frame.core :as rf]
            [tiles.views.tile :refer [tile]]))

(def board-query
  (d/parse-document
   "{
      legend {
        selected { slug }
      }
      currentBoard {
        slug
        rows {
          slug
          rowTiles {
            slug
            tile {
              color
              backgroundColor
            }
          }
        }
      }
    }"))

(defn handle-click
  "Replaces the tile with the selected tile in the legend"
  [row-tile-slug selected-tile-slug]
  (rf/dispatch [:write-fragment
                {:tile { :slug selected-tile-slug}}
                (d/parse-document
                 "fragment rowTile on RowTile {
                    tile { slug }
                  }")
                row-tile-slug]))

(defn board []
  (let [{:keys [legend currentBoard]} @(rf/subscribe [:read board-query])]
    [:div {:style {:display "flex"
                   :flex-direction "column"
                   :justify-content "center"}}
     (for [row (:rows currentBoard)]
       ^{:key (:slug row)}
       [:div {:style {:display "flex"}}
        (for [row-tile (:rowTiles row)]
          ^{:key (:slug row-tile)}
          [tile {:on-click         #(handle-click (:slug row-tile)
                                                  (-> legend :selected :slug))
                 :color            (-> row-tile :tile :color)
                 :background-color (-> row-tile :tile :backgroundColor)}])])]))
