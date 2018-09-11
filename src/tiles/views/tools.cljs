(ns tiles.views.tools
  (:require [artemis.document :as d]
            [tiles.store :as s]
            [re-frame.core :as rf]))

(defn option [{:keys [text on-click]}]
  [:div {:on-click on-click
         :style {:font-size 16
                 :color "#131313"
                 :font-family "Georgia"}}
   text])

(defn set-active-board [slug]
  (rf/dispatch [:write
                {:currentBoard {:slug slug}}
                (d/parse-document "{ currentBoard { slug } }")]))

(defn create-new-board [_]
  (let [{:keys [slug] :as new-board} (s/new-blank-board)
        ref [:slug slug]]
    (rf/dispatch [:write-fragment
                  new-board
                  (d/parse-document "fragment boardFrag on Board {
                      __typename
                      slug
                      rows {
                        __typename
                        slug
                        rowTiles {
                          __typename
                          slug
                          tile {__typename slug backgroundColor color}}
                      }}")
                  ref])
    (rf/dispatch [:update-store
                  (fn [store]
                    (-> store
                        (update-in [:entities "root" :boards]
                                   conj
                                   {:artemis.mapgraph/ref ref})
                        (assoc-in [:entities "root" :currentBoard]
                                   {:artemis.mapgraph/ref ref})))])))

(defn links []
  (let [query (d/parse-document "{ boards { slug }}")
        slugs (:boards @(rf/subscribe [:read query]))]
    [:div
     (for [{:keys [slug]} slugs]
       ^{:key slug}
       [:div {:on-click (fn [e]
                          (set-active-board slug))}
        slug])]))

(defn tools []
  (let []
    [:div {:style {:position        "fixed"
                   :left 0
                   :height          "100%"
                   :display         "flex"
                   :justify-content "center"
                   :align-items     "center"}}
     [:div {:style {:height 500
                    :border-left "5px solid #ffeb3b"
                    :padding-left 10}}
      [option {:text "New" :on-click create-new-board}]
      [links]]]))
