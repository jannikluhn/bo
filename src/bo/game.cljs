(ns bo.game
  (:require [clojure.string :as str]
            [goog.string :as gstring]
            [goog.string.format]
            [bo.logic :as logic :refer [init-game]]
            [reagent.core :as r]))

(defonce game-state (r/atom (init-game)))

;
; Actions
;
(defn stop-game []
  (swap! game-state logic/stop-game))

(defn start-game []
  (swap! game-state logic/start-game 30)
  (js/setTimeout stop-game (* (::logic/duration @game-state) 1000)))

(defn sort-left []
  (swap! game-state logic/take-guess false))

(defn sort-right []
  (swap! game-state logic/take-guess true))

;
; Key event handlers
;
(defn window-event-handler [event-name handler]
  (r/create-class
    {:display-name (str event-name "-window-handler")
     :component-did-mount (fn [this] (.addEventListener js/window event-name handler))
     :component-will-unmount (fn [this] (.removeEventListener js/window event-name handler))
     :reagent-render (fn [this] nil)}))

(defn start-game-handler []
  (window-event-handler "keypress"
                        (fn [e] (when (= (.-key e) "Enter") (start-game)))))

(defn guess-handler []
  (window-event-handler "keydown"
                        (fn [e] (case (.-key e)
                                  "ArrowLeft" (sort-left)
                                  "ArrowRight" (sort-right)
                                  nil))))

(defn stop-handler []
  (window-event-handler "keypress"
                        (fn [e] (println (.-key e)))))

(defn circle-attrs [size]
  {:cx 50
   :cy 50
   :r (/ size 1.77)})

(defn square-attrs [size]
  {:x (- 50 (/ size 2))
   :y (- 50 (/ size 2))
   :width size
   :height size})

(defn path-def-str [& points]
  (let [ps "%.2f %.2f"
        s (str "M "
               (str/join " L " (repeat (count points) ps))
               " Z")
        coords (flatten points)]
    (apply (partial gstring/format s) coords)))

(defn diamond-attrs [size]
  (let [half-diag (/ (* 1.41 size) 2)]
    {:d (path-def-str [50 (- 50 half-diag)]
                      [(- 50 half-diag) 50]
                      [50 (+ 50 half-diag)]
                      [(+ 50 half-diag) 50])}))

(defn fill-pattern-id [color-value]
  (gstring/format "pattern%d" color-value))

(defn fill-pattern [color id]
  [:pattern#pattern
   {:id id
    :patternContentUnits "objectBoundingBox"
    :width 1
    :height 1}
   [:polygon {:points "1,0 0,1 1,1"
              :fill color}]])

(defn card-svg [c]
  (fn [[shape-value color-value size-value fill-value]]
    (let [element (case shape-value
                    0 :circle
                    1 :rect
                    2 :path)
          shape-attr-fn (case shape-value
                          0 circle-attrs
                          1 square-attrs
                          2 diamond-attrs)
          size (case size-value
                 0 20
                 1 40
                 2 60)
          shape-attrs (shape-attr-fn size)

          color (case color-value
                  0 "#4c51bf"
                  1 "#2f855a"
                  2 "#c53030")
          pattern-id (fill-pattern-id color-value)
          fill (case fill-value
                 0 "none"
                 1 (gstring/format "url(#%s)" pattern-id)
                 2 color)
          style-attrs {:stroke color
                       :stroke-width 10
                       :vector-effect "non-scaling-stroke"
                       :fill fill}

          pattern (case fill-value
                    1 (fill-pattern color pattern-id)
                    nil)

          card-attrs (merge shape-attrs style-attrs)
          card [element card-attrs]]
      [:div
       [:svg {:width "100%" :height "100%" :view-box "0 0 100 100"}
        [:defs pattern]
        card]])))

(defn ms->s [ms]
  (/ ms 1000))

(defn s->ms [s]
  (* s 1000))

(defn count-down [until update-interval]
  (let [seconds-left #(- until (ms->s (js/Date.now)))
        comp-state (r/atom {::seconds-left (seconds-left)})]
    (r/create-class
      {:display-name "count-down"
       :component-did-mount
       (fn [this]
         (swap! comp-state assoc ::interval-id
                (js/setInterval
                  (fn [] (swap! comp-state update ::seconds-left seconds-left))
                  (s->ms update-interval)))
         (js/setTimeout #(js/clearInterval (::interval-id @comp-state))
                        (s->ms (::seconds-left @comp-state)))) :reagent-render
       (fn [until update-interval]
         [:div
          [:p "Time left: " [:strong (gstring/format "%.1f s" (::seconds-left @comp-state))]]])})))

(defn card [c]
  [:div.m-4.shadow-xl.border.rounded-lg.border-gray-500.md:max-w-full [card-svg c]])

(defn three-cards [cards]
  [:div.container.flex.flex-col.md:flex-row.items-center.max-h-screen
   (for [[i c] (map-indexed vector cards)]
     ^{:key i} [card c])])

(defn sort-buttons []
  [:div.flex.justify-center
   [:button.w-32.p-4.m-2.bg-orange-400.rounded.shadow-xl.hover:bg-orange-500
    {:on-click sort-left}
    "🠈 No Bo"]
   [:button.w-32.p-4.m-2.bg-orange-400.rounded.shadow-xl.hover:bg-orange-500
    {:on-click sort-right}
    "Bo 🠊"]])

(defn controls-and-score []
  [:div.container.flex.items-center.px-8
   [:div.flex-1
    [count-down (+ (ms->s (js/Date.now)) (@game-state ::logic/duration)) 0.1]]
   [:div.flex-1
    [sort-buttons]]
   [:div.flex-1.flex.justify-end
    [:p "Score: " [:strong (@game-state ::logic/score)]]]])

(defn started-game []
  [:div
   [guess-handler]
   [stop-handler]
   [three-cards (::logic/challenge @game-state)]
   [controls-and-score]])

(defn initialized-game []
  [:div.container
   [start-game-handler]
   [:p.text-2xl.text-center
    "Hit " [:kbd "enter"] " to start the game"]])

(defn stopped-game []
  [:div.game
   [start-game-handler]
   [:p.text-2xl.text-center "Time's up!"]
   [:p.text-2xl.text-center "Final score: " (@game-state ::logic/score)]
   [:p.text-2xl.text-center "Hit " [:kbd "enter"] " to go again"]])

(defn game []
  [:div
   [:h1.text-4xl.font-extrabold.text-center.mt-8.mb-5 "Sort Mode"]
     (case (::logic/phase @game-state)
            ::logic/initialized [initialized-game]
            ::logic/started [started-game]
            ::logic/stopped [stopped-game])])
