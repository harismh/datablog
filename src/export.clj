(ns export
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [hiccup2.core :as h]
   [datascript.core :as d]))

(def site-eid 1)

(def *conn (d/create-conn {}))

(defn txs []
  (->>
   (file-seq (io/file "./public/data"))

   (filter
    #(let [name (.getName %)]
       (and
        (not (str/starts-with? name "_"))
        (str/ends-with? name ".edn"))))

   (map #(read-string (slurp %)))))

(defn seed []
  (doseq [tx (txs)]
    (d/transact! *conn tx)))

(defn entries [db]
  (->>
   (d/q
    '[:find ?e
      :where [?e :entry/type]]
    db)

   (map #(d/entity db (first %)))

   (sort-by
    (fn [ent] (.getTime (:entry/created-at ent)))
    >)))

(defn site-meta [db]
  (d/entity db site-eid))

(defn index [db]
  (let [{:site/keys [title author]} (site-meta db)
        ents (entries db)]
    [:html {:lang "en"}
     [:head
      [:title title]
      [:meta {:name "viewport" :content "width=device-width"}]
      [:meta {:name "author" :content author}]]
     [:body
      [:h1 title]
      [:h2 "Entries"]
      [:ul
       (for [{:entry/keys [title slug]} ents]
         [:li [:a
               {:href (str "/entries" slug)}
               title]])]]]))

(defn gen-html [db]
  (str
   "<!doctype html>"
   (h/html
    (index db))))

(defn -main []
  (seed)
  (spit
   "index.html"
   (gen-html (deref *conn)))
  (println "Export finished."))