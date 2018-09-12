(ns tiles.views.sidebar
  (:require [artemis.document :as d]
            [tiles.store :as s]
            [re-frame.core :as rf]))

(defn action [{:keys [text on-click]}]
  [:button {:class    "Action"
            :on-click on-click}
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
        slugs (:boards @(rf/subscribe [:read query]))
        active-slug (-> @(rf/subscribe [:read (d/parse-document "{ currentBoard { slug } }")])
                        :currentBoard :slug)]
    [:div
     (for [{:keys [slug]} slugs]
       ^{:key slug}
       [:div {:class    ["Link" (when (= slug active-slug) "active")]
              :on-click #(set-active-board slug)}
        slug])]))

(defn sidebar []
  (let []
    [:div {:class "Sidebar"}
     [:div {:class "Sidebar-container"}
      [:div {:class "Logo"} "Tiles."]
      [:div {:class "Tools"}
       [action {:text "new" :on-click create-new-board}]
       [action {:text "undo" :on-click #(rf/dispatch [:undo])}]
       [action {:text "redo" :on-click #(rf/dispatch [:redo])}]]
      [:div {:class "Links"}
       [links]]]]))
