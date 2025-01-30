(ns site.db
  (:require
   [datascript.core :as d]
   [clojure.string :as str]
   [site.utils :as u]))

(def schema
  {:site/title          {}
   :site/author         {}
   :site/copyright-year {}
   :site/email          {}
   :site/github         {}
   :site/bluesky        {}
   :site/hero-body      {}
   :site/hero-image     {}
   :site/current-route  {}
   :site/current-slug   {}
   :site/current-type   {}
   :site/current-search {}
   :site/seed-ms        {}

   :entry/type          {}
   :entry/slug          {}
   :entry/created-at    {}
   :entry/title         {}
   :entry/summary       {}
   :entry/body          {}
   :entry/external-link {}})

(def site-eid 1)

(defn seed [conn]
  (let [seed'
        (fn []
          (u/fetch-edn
           "/data/_index.edn"
           (fn [index]
             (doseq [url index]
               (u/fetch-edn
                url
                (fn [txs] (d/transact! conn (reverse txs))))))))
        ms (u/timev (seed'))]
    (d/transact!
     conn
     [{:db/id site-eid
       :site/seed-ms ms}])))

(defn site-meta [db]
  (d/entity db site-eid))

(defn entry [db slug]
  (->>
   (d/q '[:find ?e .
          :in $ ?slug
          :where [?e :entry/slug ?slug]]
        db slug)
   (d/entity db)))

(defn entries
  ([db]
   (entries db nil))
  ([db type]
   (->>
    (d/q
     (if type ; workaround, would like to pass in unquoted _
       '[:find ?e
         :in $ ?type
         :where [?e :entry/type ?type]]
       '[:find ?e
         :in $ ?type
         :where [?e :entry/type]])
     db type)

    (map #(d/entity db (first %)))

    (sort-by
     #(.getTime (:entry/created-at %))
     >))))

(defn search-entries [db query]
  (let [rows       (d/q
                    '[:find ?e ?t
                      :where
                      [?e :entry/title ?t]]
                    db)
        normalized (map (fn [[e t]] [e (str/lower-case t)]) rows)
        matches    (filter (fn [[_ t]] (str/includes? t query)) normalized)]
    (when (seq matches)
      (map (fn [[e]] (d/entity db e)) matches))))