(ns bo.page
  (:require [reagent.core :as r]
            [accountant.core :as accountant]
            [secretary.core :as secretary :refer-macros [defroute]]
            [accountant.core :as accountant]
            [bo.game :refer [game]]
            [bo.help :refer [help]]))

(def app-state (r/atom {::page ::game-page}))

(def page-map
  {::game-page game
   ::help-page help})

(defn navbar []
  [:nav
   [:div.flex.items-center.justify-around.bg-orange-300.py-6.border-b
    [:div.flex-grow.max-w-4xl.flex.justify-between.items-center
     [:a.m-2.font-bold.text-2xl {:href "/"} "Bo Game"]
     [:div
      [:a.mx-2 {:href "/help"} "Help"]]]]])

(defn content []
  [:div.flex.items-center.justify-around.bg-orange-100
   [:div.flex-grow.max-w-4xl.min-h-screen.pb-16.flex.justify-center
    [((::page @app-state) page-map)]]])

(defn page []
  [:div.flex.flex-col
   [navbar]
   [content]])

;
; Routes
;
(defroute game-path "/" []
  (swap! app-state assoc ::page ::game-page))

(defroute help-path "/help" []
  (swap! app-state assoc ::page ::help-page))

(accountant/configure-navigation!
  {:nav-handler secretary/dispatch!
   :path-exists? secretary/locate-route})
