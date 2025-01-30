(ns site.main
  (:require
   [datascript.core :as d]
   [replicant.dom :as replicant]
   [site.db :as db]
   [site.router :as router]
   [site.ui :as ui]
   [site.utils :as u]))

(defonce *render-ms (atom nil))

(defonce *conn (d/create-conn db/schema))

(router/start! *conn)

(defn handle-select [key value]
  (case key
    :site/current-type
    (d/transact!
     *conn
     [{:db/id db/site-eid
       :site/current-type value}])
    nil))

(defn handle-text [key value]
  (case key
    :site/current-search
    (d/transact!
     *conn
     [{:db/id db/site-eid
       :site/current-search value}])
    nil))

(defn dispatch
  [{:replicant/keys [node]} [event & payload]]
  (case event
    :input/select
    (handle-select
     (first payload)
     (keyword (ui/select-value node)))

    :input/text
    (handle-text
     (first payload)
     (ui/input-value node))

    :alert
    (js/window.alert (first payload))
    nil))

(defn render [db]
  (let [render'
        (fn []
          (replicant/render
           (js/document.getElementById "root")
           (ui/shell db (deref *render-ms))))
        ms (u/timev (render'))]
    (reset! *render-ms ms)))

(defn ^:export ^:dev/after-load init
  []
  (replicant/set-dispatch! dispatch)
  (db/seed *conn)
  (render (deref *conn))
  (add-watch
   *conn ::re-render
   (fn [_ _ _ db'] (render db'))))