(ns tiles.store
  (:require [re-frame.core :as rf]
            [artemis.core :as a]
            [artemis.document :as d]
            [artemis.stores.mapgraph.core :as mgs]
            [artemis.stores.mapgraph.read :as mgr]))

(def blank-tile
  "Our blank tile, used in colors and the blank-board creation"
  {:__typename "Tile" :backgroundColor "red"     :color "white"})

(def colors
  "The palette of colors available by default"
  [blank-tile
   {:__typename "Tile" :backgroundColor "#444" :color "white"}
   {:__typename "Tile" :backgroundColor "blue" :color "white"}
   {:__typename "Tile" :backgroundColor "cyan" :color "blue"}
   {:__typename "Tile" :backgroundColor "red" :color "white"}
   {:__typename "Tile" :backgroundColor "pink" :color "white"}
   {:__typename "Tile" :backgroundColor "yellow" :color "red"}
   {:__typename "Tile" :backgroundColor "#64c7cc" :color "cyan"}
   {:__typename "Tile" :backgroundColor "#00a64d" :color "#75f0c3"}
   {:__typename "Tile" :backgroundColor "#f5008b" :color "#ffdbbf"}
   {:__typename "Tile" :backgroundColor "#0469bd" :color "#75d2fa"}
   {:__typename "Tile" :backgroundColor "#fcf000" :color "#d60000"}
   {:__typename "Tile" :backgroundColor "#010103" :color "#fa8e66"}
   {:__typename "Tile" :backgroundColor "#7a2c02" :color "#fff3e6"}
   {:__typename "Tile" :backgroundColor "white" :color "red"}
   {:__typename "Tile" :backgroundColor "#f5989c" :color "#963e03"}
   {:__typename "Tile" :backgroundColor "#ed1c23" :color "#fff780"}
   {:__typename "Tile" :backgroundColor "#f7f7f7" :color "#009e4c"}
   {:__typename "Tile" :backgroundColor "#e04696" :color "#9c2c4b"}])

(def blank-board
  "This is the initial blank board"
  (let [row (vec (repeat 15 blank-tile))]
    {:__typename "Board"
     :slug "blank"
     :rows (vec (repeatedly 20 (fn []
                                 {:__typename "Row"
                                  :slug (random-uuid)
                                  :tiles row})))}))

(defn id-fn
  "Our ID function used by artemis to normalize whatever entity it comes across"
  [{:keys [__typename slug] :as entity}]
  (case __typename
    "Tile"
    (clojure.string/join "/" ((juxt :backgroundColor :color) entity))

    slug))

(def client
  "Creating the artemis client with the mapgraph store, for now
  our ID function is pretty basic, defaults to an entities slug field,
  That function is used to normalize our data internally"
  (a/create-client :store (mgs/create-store :id-fn id-fn)))

;; Basic subscription to subscribe to store changes
(rf/reg-sub :store :store)

(rf/reg-event-db
 :write
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

(rf/reg-event-db
 :initialize
 (fn [_ _]
   ;; TODO: Mocking the server data by hand, replace with call to GraphQL endpoint
   (let [store (-> (a/store client)
                   ;; write the bank of tiles to db
                   (a/write {:data {:tiles colors}}
                            (d/parse-document "{tiles {__typename backgroundColor color}}")
                            {})

                   ;; write the default board
                   (a/write {:data {:boards [blank-board]}}
                            (d/parse-document "{boards {__typename
                                                        slug
                                                        rows {
                                                          __typename
                                                          slug
                                                          tiles {__typename backgroundColor color}
                                                        }}}")
                            {}))]
     {:store store})))

(comment
  ;; repl testing
  (-> @(rf/subscribe [:store]) :entities)
  @(rf/subscribe [:pull [:slug :__typename {:rows [:slug]}] "blank"])
  @(rf/subscribe [:read tile-query]))
