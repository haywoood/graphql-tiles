(ns tiles.store
  (:require [artemis.core :as a]
            [artemis.document :as d]
            [artemis.stores.mapgraph.core :as mgs]
            [artemis.stores.mapgraph.read :as mgr]
            [day8.re-frame.undo :as undo :refer [undoable]]
            [debux.cs.core :refer-macros [clog]]
            [re-frame.core :as rf]))

(defn gen-slug []
  (.toString (random-uuid)))

(def blank-tile
  "Our blank tile, used in colors and the blank-board creation"
  {:__typename "Tile" :slug (gen-slug) :backgroundColor "white" :color "red"})

(def colors
  "The palette of colors available by default"
  [{:__typename "Tile" :slug (gen-slug) :backgroundColor "#444" :color "white"}
   {:__typename "Tile" :slug (gen-slug) :backgroundColor "blue" :color "white"}
   {:__typename "Tile" :slug (gen-slug) :backgroundColor "cyan" :color "blue"}
   {:__typename "Tile" :slug (gen-slug) :backgroundColor "red" :color "white"}
   {:__typename "Tile" :slug (gen-slug) :backgroundColor "pink" :color "white"}
   {:__typename "Tile" :slug (gen-slug) :backgroundColor "yellow" :color "red"}
   {:__typename "Tile" :slug (gen-slug) :backgroundColor "#64c7cc" :color "cyan"}
   {:__typename "Tile" :slug (gen-slug) :backgroundColor "#00a64d" :color "#75f0c3"}
   {:__typename "Tile" :slug (gen-slug) :backgroundColor "#f5008b" :color "#ffdbbf"}
   {:__typename "Tile" :slug (gen-slug) :backgroundColor "#0469bd" :color "#75d2fa"}
   {:__typename "Tile" :slug (gen-slug) :backgroundColor "#fcf000" :color "#d60000"}
   {:__typename "Tile" :slug (gen-slug) :backgroundColor "#010103" :color "#fa8e66"}
   {:__typename "Tile" :slug (gen-slug) :backgroundColor "#7a2c02" :color "#fff3e6"}
   {:__typename "Tile" :slug (gen-slug) :backgroundColor "#f5989c" :color "#963e03"}
   {:__typename "Tile" :slug (gen-slug) :backgroundColor "#ed1c23" :color "#fff780"}
   {:__typename "Tile" :slug (gen-slug) :backgroundColor "#f7f7f7" :color "#009e4c"}
   {:__typename "Tile" :slug (gen-slug) :backgroundColor "#e04696" :color "#9c2c4b"}])

(defn blank-board
  "This is the initial blank board"
  []
  {:__typename "Board"
   :slug       (gen-slug)
   :rows       (vec (repeatedly 8
                                (fn []
                                  {:__typename "Row"
                                   :slug       (gen-slug)
                                   :rowTiles   (repeatedly 10
                                                           (fn []
                                                             {:__typename "RowTile"
                                                              :slug       (gen-slug)
                                                              :tile       blank-tile}))})))})

(defn new-blank-board
  "Helper used to create a new board with a unique slug"
  []
  (assoc (blank-board) :slug (gen-slug)))

(defn id-fn
  "Our ID function used by artemis to normalize whatever entity it comes across
  currently relies on entities having unique slugs, may change in the future"
  [{:keys [slug]}]
  [:slug slug])

(def client
  "Creating the artemis client with the mapgraph store, for now
  our ID function is pretty basic, defaults to an entity's slug field,
  That function is used to normalize our data internally"
  (a/create-client :store (mgs/create-store :id-fn id-fn)))

;; Basic subscription to subscribe to store changes
(rf/reg-sub :store :store)

(rf/reg-event-db
 :update-store
 (undoable "manual store update")
 (fn [db [_ fn]]
   "Escape hatch when we bump into limitations of artemis.
   Like manually push a ref onto the boards vector"
   (update db :store fn)))

(rf/reg-event-db
 :write-fragment
 (undoable "write fragment")
 (fn [db [_ data query-fragment entity-ref]]
   "Update an entity in the store by targeting the ref directly"
   (update db :store (fn [store]
                       (a/write-fragment store
                                         {:data data}
                                         query-fragment
                                         [:slug entity-ref])))))

(rf/reg-event-db
 :write
 (undoable "write query")
 (fn [db [_ data query-doc vars]]
   "Update the local store based on a GraphQL query"
   (update db :store (fn [store]
                       (a/write store {:data data} query-doc (or vars {}))))))

(rf/reg-sub
 :read
 (fn [db [_ query-doc vars]]
   "Query the local store using a GraphQL Query"
   (:data (a/read (:store db) query-doc (or vars {})))))

(rf/reg-sub
 :pull
 (fn [db [_ pull ref]]
   "Query the store using a pull query, if no ref is given, queries on 'root'"
   (mgr/pull (:store db) pull {:artemis.mapgraph/ref (or ref "root")})))

;; TODO: Mocking the server data by hand, replace with call to GraphQL endpoint
;; TODO: Remove duplication in queries by creating fragments, may be worth it later
(rf/reg-event-db
 :initialize
 (fn [_ _]
   (let [default-board (blank-board)
         store (->
                ;; Thread the store through bootstrapping writes
                (a/store client)

                ;; write the bank of tiles to db
                (a/write {:data {:tiles (conj colors blank-tile)}}
                         (d/parse-document "{tiles {__typename slug backgroundColor color}}")
                         {})

                ;; write the legend tiles using colors var
                (a/write {:data {:legend {:__typename "Legend"
                                          :slug       "legend"
                                          :tiles      colors
                                          :selected   (first colors)}}}
                         (d/parse-document "{legend {__typename
                                                     slug
                                                     selected {__typename slug backgroundColor color}
                                                     tiles {__typename slug backgroundColor color}}}")
                         {})

                ;; write the default board
                (a/write {:data {:boards [default-board]
                                 :currentBoard (select-keys default-board [:slug])}}
                         (d/parse-document "{currentBoard {__typename
                                                            slug
                                                            rows {
                                                              __typename
                                                              slug
                                                              rowTiles {
                                                                __typename
                                                                slug
                                                                tile {__typename slug backgroundColor color}}
                                                            }}
                                             boards {__typename
                                                     slug
                                                     rows {
                                                       __typename
                                                       slug
                                                       rowTiles {
                                                         __typename
                                                         slug
                                                         tile {__typename slug backgroundColor color}}
                                                     }}}")
                         {}))]

     ;; Initial app-db with normalized data graph
     {:store store})))
