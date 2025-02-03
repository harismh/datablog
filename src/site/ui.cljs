(ns site.ui
  (:require
   [site.db :as db]
   [site.utils :as u]))

(defn anchor [{:keys [href target]} & body]
  [:a {:href href
       :target target}
   body])

(defn button [{:keys [href]} & body]
  (anchor
   {:href href}
   [:button body]))

(defn select
  ([key options]
   (select key options nil))
  ([key options value]
   [:div.select-container
    [:select.select
     {:on {:change [:input/select key]}
      :value value}
     (for [{:keys [value label]} options]
       [:option {:value value} label])]]))

(defn select-value [select-el]
  (-> select-el
      .-options
      (aget
       (.-selectedIndex select-el))
      .-value))

(defn input [{:keys [key value placeholder]}]
  [:input
   {:on {:input [:input/text key]}
    :value value
    :placeholder placeholder}])

(defn input-value [input-el]
  (.-value input-el))

(defn section [& body]
  [:section
   {:style
    {:display "flex"
     :flex-direction "column"
     :gap "2rem"
     :padding "0rem 2rem"}}
   body])

(defn article [& body]
  [:article
   {:style
    {:display "flex"
     :flex-direction "column"
     :gap "2rem"
     :padding "0rem 2rem"}}
   body])

(defn entry-tile [{:entry/keys [type title summary slug created-at]}]
  [:a
   {:href (str "/entries/" slug)
    :style {:display "flex"
            :flex-direction "column"
            :gap "1rem"
            :background-color "var(--theme-tile-background)"
            :border-radius "1rem"
            :padding "2rem"
            :min-width "var(--tile-width)"
            :min-height "var(--tile-height)"
            :max-width "var(--tile-width)"
            :max-height "var(--tile-height)"
            :cursor "pointer"
            :user-select "none"
            :-webkit-user-select "none"}}
   [:div
    {:style {:display "flex"
             :flex-direction "column"
             :gap "0.25rem"
             :flex "1 1 10%"}}
    [:h3 {:style {}} title]
    [:div
     {:style {:display "flex"
              :flex-direction "row"
              :justify-content "space-between"
              :gap "1rem"}}
     [:pre type]
     [:pre (u/format-date created-at)]]]
   [:p.truncate-4 {:style {:flex "2"}} summary]])

(defn invert-scroll [node]
  (.addEventListener
   node
   "wheel"
   (fn [e]
     (let [y (.-deltaY e)
           x (.-deltaX e)
           s (.-scrollLeft (.-currentTarget e))]
       (when (not= 0 y)
         (.preventDefault e)
         (set! (.-scrollLeft (.-currentTarget e))
               (+ s y x)))))))

(defn gallery [db]
  [:div.gallery-scroll
   {:replicant/on-mount
    (fn [{:replicant/keys [node]}]
      (invert-scroll node))
    :style {:overflow-y "scroll"
            :display "flex"
            :gap "1rem"}}
   (for [entry (db/entries db)]
     (entry-tile entry))])

(defn footer [db render-ms]
  (let [meta (db/site-meta db)
        copyright (:site/copyright-year meta)
        email (:site/email meta)
        github (:site/github meta)
        bluesky (:site/bluesky meta)]
    [:footer
     {:style
      {:display "flex"
       :align-items "center"
       :justify-content "center"
       :color "var(--theme-tertiary)"}}
     [:div
      {:style
       {:display "flex"
        :gap "2rem"
        :max-width "var(--max-content-width)"}}
      [:div
       [:pre "Render Time: " (u/round render-ms) " ms"]]
      [:div
       {:style {:display "flex"
                :align-items "center"
                :gap "0.5rem"}}
       (when email
         [:a {:href (str "mailto:" email)}
          [:svg {:style {:transform "scale(0.7)"
                         :position "relative"
                         :top -0.5}
                 :stroke "currentColor"
                 :fill "none"
                 :stroke-linejoin "round"
                 :width "24"
                 :viewbox "0 0 24 24"
                 :xmlns "http://www.w3.org/2000/svg"
                 :stroke-linecap "round"
                 :stroke-width "1.5"
                 :height "24"}
           [:rect {:width "20" :height "16" :x "2" :y "4" :rx "2"}]
           [:path {:d "m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"}]]])
       (when github
         [:a.no-target-arrow
          {:href (str "https://github.com/" github)
           :target "_blank"}
          [:svg {:style {:transform "scale(0.7)"
                         :position "relative"
                         :top -0.5}
                 :stroke "currentColor"
                 :fill "none"
                 :stroke-linejoin "round"
                 :width "24"
                 :viewbox "0 0 24 24"
                 :xmlns "http://www.w3.org/2000/svg"
                 :stroke-linecap "round"
                 :stroke-width "1.5"
                 :height "24"}
           [:path {:d "M15 22v-4a4.8 4.8 0 0 0-1-3.5c3 0 6-2 6-5.5.08-1.25-.27-2.48-1-3.5.28-1.15.28-2.35 0-3.5 0 0-1 0-3 1.5-2.64-.5-5.36-.5-8 0C6 2 5 2 5 2c-.3 1.15-.3 2.35 0 3.5A5.403 5.403 0 0 0 4 9c0 3.5 3 5.5 6 5.5-.39.49-.68 1.05-.85 1.65-.17.6-.22 1.23-.15 1.85v4"}]
           [:path {:d "M9 18c-4.51 2-5-2-7-2"}]]])
       (when bluesky
         [:a.no-target-arrow
          {:href (str "https:bsky.app/profile/" bluesky)
           :target "_blank"}
          [:svg {:style {:transform "scale(1.2)"
                         :position "relative"
                         :top 0
                         :left 2}
                 :stroke "currentColor"
                 :stroke-width "0.9"
                 :fill "none"
                 :stroke-linejoin "round"
                 :width "16"
                 :viewbox "0 0 16 16"
                 :xmlns "http://www.w3.org/2000/svg"
                 :stroke-linecap "round"
                 :height "16"}
           [:path {:d "M3.959375 3.215C2.925625 2.4656249999999997 1.25 1.8856249999999999 1.25 3.7312499999999997c0 0.36874999999999997 0.21875 3.095625 0.34750000000000003 3.538125C2.043125 8.80875 3.55375 8.988125 5 8.75c-2.528125 0.415625 -3.055625 2.005 -1.6668749999999999 3.38125C3.976875 12.7675 4.5287500000000005 13.125 5 13.125c1.25 0 1.95875 -1.730625 2.1875 -2.1875 0.208125 -0.416875 0.3125 -0.729375 0.3125 -0.9375 0 0.208125 0.10437500000000001 0.520625 0.3125 0.9375 0.22875 0.456875 0.9375 2.1875 2.1875 2.1875 0.47125 0 1.023125 -0.35687499999999994 1.6668749999999999 -0.99375C13.055625 10.754375 12.528125000000001 9.165 10 8.75c1.44625 0.2375 2.9562500000000003 0.05875 3.4025 -1.480625 0.12875 -0.4425 0.34750000000000003 -3.17 0.34750000000000003 -3.538125 0 -1.8456249999999998 -1.675 -1.265625 -2.709375 -0.51625C9.6075 4.25375 8.065624999999999 6.37 7.5 7.5c-0.565625 -1.1300000000000001 -2.1075 -3.24625 -3.540625 -4.285z"}]]])
       (when copyright
         [:span
          {:style {:padding-left "0.5rem"
                   :position "relative"
                   :top -2}}
          (str "© " copyright)])]]]))

(defn home [db]
  (let [meta (db/site-meta db)]
    (list
     (section
      [:div.r-stack.hero
       {:style
        {:align-items "center"
         :justify-content "space-between"
         :gap "3rem"}}
       [:div
        (:site/hero-image meta)]
       (:site/hero-body meta)])
     (section
      [:div
       {:style
        {:display "flex"
         :align-items "center"
         :justify-content "space-between"}}
       [:h2 "Latest"]
       (button {:href "/entries"} "View All")]
      (gallery db)))))

(defn entry [db]
  (let [slug  (:site/current-slug (db/site-meta db))
        {:entry/keys [title body summary
                      external-link created-at]} (db/entry db slug)
        text (or body summary)
        content (if (string? text) [:p text] text)]
    [:div
     {:style {:padding "0rem 2rem"}}
     (article
      [:span
       {:style {:display "flex"
                :flex-direction "column"
                :gap "0.25rem"}}
       [:h1 title]
       [:pre (u/format-date created-at)]]
      content
      (when external-link
        [:span
         {:style
          {:display "flex"}}
         (anchor
          {:href external-link
           :target "_blank"}
          "View Source")]))]))

(defn entry-list [{:entry/keys [type title slug created-at]}]
  [:div
   {:style
    {:display "flex"
     :align-items "center"
     :justify-content "space-between"
     :gap "4rem"}}
   (anchor
    {:href (str "/entries/" slug)}
    [:p title])
   [:div
    {:style
     {:display "flex"
      :gap "1rem"}}
    [:pre type]
    [:pre (u/format-date created-at)]]])

(defn entries [db]
  (let [type    (:site/current-type (db/site-meta db))
        entries (db/entries db (if (= type :all) nil type))]
    [:div.list
     {:style {:padding "0rem 2rem"}}
     [:div
      {:style
       {:display "flex"
        :flex-direction "column"
        :gap "0.5rem"
        :padding "0rem 2rem"}}
      [:div
       {:style
        {:display "flex"
         :align-items "center"
         :justify-content "space-between"
         :margin-bottom "2rem"}}
       [:h1 "Entries"]
       (select :site/current-type
               [{:value :all :label "All"}
                {:value :essay :label "Essays"}
                {:value :link :label "Links"}
                {:value :note :label "Notes"}]
               type)]
      (for [entry entries] (entry-list entry))]]))

(defn search [db]
  (let [query   (:site/current-search (db/site-meta db))
        filled  (boolean (seq query))
        entries (when filled (db/search-entries db query))]
    [:div.list
     {:style {:padding "0rem 4rem"}}
     [:div
      {:style
       {:display "flex"
        :flex-direction "column"
        :gap "0.5rem"
        :padding "0rem 2rem"}}
      [:h1 "Search"]
      (input {:key :site/current-search
              :value query
              :placeholder "Search entries..."})
      [:div
       {:style
        {:display "flex"
         :margin-top "2rem"
         :flex-direction "column"
         :gap "0.5rem"}}
       (if entries
         (for [entry entries] (entry-list entry))
         (when filled [:em "No results for \"" query "\""]))]]]))

(defn main [db route]
  [:main
   {:style
    {:display "flex"
     :flex-direction "column"
     :align-items "center"
     :justify-content "center"
     :gap "4rem"
     :padding "4rem 0rem"}}
   (case route
     :route/search (search db)
     :route/entries (entries db)
     :route/entry (entry db)
     (home db))])

(defn header []
  [:header.blur
   {:style {:display "flex"
            :position "sticky"
            :top 0
            :flex-direction "row"
            :align-items "center"
            :justify-content "center"
            :gap "4rem"
            :z-index 1
            :transition "backdrop-filter 0.2s ease"}}
   (anchor
    {:href "/"}
    [:span
     [:svg
      {:style
       {:position "relative"
        :top 2
        :transform "scale(0.8)"}
       :stroke "currentColor"
       :fill "none"
       :stroke-linejoin "round"
       :width "24"
       :viewbox "0 0 24 24"
       :xmlns "http://www.w3.org/2000/svg"
       :stroke-linecap "round"
       :stroke-width "1.2"
       :height "24"}
      [:path {:d "M12 12V4a1 1 0 0 1 1-1h6.297a1 1 0 0 1 .651 1.759l-4.696 4.025"}]
      [:path {:d "m12 21-7.414-7.414A2 2 0 0 1 4 12.172V6.415a1.002 1.002 0 0 1 1.707-.707L20 20.009"}]
      [:path {:d "m12.214 3.381 8.414 14.966a1 1 0 0 1-.167 1.199l-1.168 1.163a1 1 0 0 1-.706.291H6.351a1 1 0 0 1-.625-.219L3.25 18.8a1 1 0 0 1 .631-1.781l4.165.027"}]]])
   (anchor {:href "/"} "Home")
   (anchor {:href "/entries"} "Entries")
   (anchor
    {:href "/search"}
    [:span
     [:svg
      {:style
       {:position "relative"
        :top 2
        :transform "scale(0.8)"}
       :stroke "currentColor"
       :fill "none"
       :stroke-linejoin "round"
       :width "24" :viewbox "0 0 24 24"
       :xmlns "http://www.w3.org/2000/svg"
       :stroke-linecap "round"
       :stroke-width "1.75" :height "24"}
      [:circle {:cx "11" :cy "11" :r "7"}]
      [:path {:d "m21 21-4.3-4.3"}]]])])

(defn shell [db render-ms]
  [:div
   {:style
    {:display "grid"
     :height "100vh"
     :grid-template-rows "50px auto 50px"
     :overflow "scroll"}}
   (header)
   (main db (:site/current-route (db/site-meta db)))
   (footer db render-ms)])