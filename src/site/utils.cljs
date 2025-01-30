(ns site.utils
  [:require (cljs.reader :refer [read-string])])

(defn fetch-edn [url callback]
  (->
   (-> (js/Promise.resolve (js/fetch url))
       (.then #(js/Promise.resolve (.text %)))
       (.catch #(js/console.error %)))
   (.then #(callback (read-string %)))))

(defn timev [& body]
  (let [time-str (with-out-str (time body))]
    (float (re-find #"[\d\.]+" time-str))))

(defn format-date [d]
  (.format
   (js/Intl.DateTimeFormat.
    "en-US"
    #js {:year "2-digit"
         :month "short"
         :day "2-digit"})
   d))

(defn round
  ([n]
   (round n 3))
  ([n precision]
   (.toPrecision (js/Number. n) precision)))