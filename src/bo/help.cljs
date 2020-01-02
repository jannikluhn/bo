(ns bo.help
  (:require [bo.game :refer [three-cards]]))

(defn property-table []
  (let [prop-names ["Shape" "Color" "Size" "Filling"]
        prop-values (apply mapv vector
                           [["Circle" "Rectangle" "Diamond"]
                            ["Red" "Green" "Blue"]
                            ["Small" "Medium" "Large"]
                            ["Empty" "Half" "Full"]])]
    (fn []
      [:div.flex.justify-center
       [:table.table-auto
        [:thead.border-b
         [:tr (for [n prop-names] [:th.px-4.py-2.text-left n])]]
        [:tbody
         (for [values prop-values]
           [:tr (for [v values] [:td.px-4.py-2 v])])]]])))

(defn help []
  [:div.max-w-2xl
   [:section
    [:h2.text-4xl.font-bold.text-center.mb-3.mt-8 "What's Bo?"]
    [:p.text-lg.m-3
     (str "Bo is a browser adaption of the tabletop card game Set. "
          "Your goal is to identify as many Bo's as fast as you can.")]]
   [:section
    [:h2.text-4xl.font-bold.text-center.mb-3.mt-8 "What's a Bo?"]
    [:p.text-lg.m-3
     (str "A Bo is a special combination of three cards. Cards display a "
          "symbol, characterized by four properties, each taking on one "
          "of three values.")]
    [property-table]
    [:p.text-lg.m-3
     (str "If the three cards have all the same or all different values "
          "for each property, they form a Bo. On the other hand, if for "
          "at least one property two cards have the same value and the "
          "third does not, they are not a Bo.")]]
   [:section
    [:h2.text-4xl.font-bold.text-center.mb-3.mt-8 "... Example?"]
   [three-cards [[0 0 0 0] [0 0 1 1] [0 0 2 2]]]
   [:p.text-lg.m-3
    (str "This is a Bo. We can see this by individually checking the four "
         "properties shape, color, size, and filling: All three symbols have "
         "the same shape (circle) and color (blue), and all three symbols "
         "differ in size and filling")]
   [three-cards [[1 0 2 0] [1 1 2 0] [1 2 2 2]]]
   [:p.text-lg.m-3
    (str "This is not a Bo: Even though shape, color, and size are fine, the "
         "first two cards are empty while the third is solid.")]]])
