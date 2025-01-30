(ns site.router
  (:require
   [datascript.core :as d]
   [reitit.frontend :as rf]
   [reitit.frontend.easy :as rfe]
   [site.db :as db]))

(def routes
  [["/"              :route/home]
   ["/search"        :route/search]
   ["/entries"       :route/entries]
   ["/entries/:slug" :route/entry]])

(def router
  (rf/router routes))

(defn start! [conn]
  (rfe/start!
   router
   (fn [match _]
     (let [route (get-in match [:data :name])
           slug (get-in match [:path-params :slug])]
       (d/transact!
        conn
        [(when route
           {:db/id db/site-eid
            :site/current-route route})
         (when slug
           {:db/id db/site-eid
            :site/current-slug slug})])))
   {:use-fragment false}))