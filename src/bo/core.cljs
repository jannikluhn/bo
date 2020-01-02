(ns bo.core
  (:require [reagent.core :as r]
            [bo.page :refer [page]]))

(defn ^:export main []
  (r/render [page]
            (.getElementById js/document "app")))
