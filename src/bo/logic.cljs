(ns bo.logic
  (:require [clojure.set :as set]))

(defn property-set? [& props]
  (let [num-props (-> props set count)]
    (or (= num-props 1) (= num-props (count props)))))

(defn bo? [& cards]
  (let [prop-sets (apply map vector cards)]
    (every? #(apply property-set? %) prop-sets)))

(defn arith-ser-sum [n]
  (/ (* n (inc n)) 2))

(defn missing-value [& values]
  (if (-> values set count (= 1))
    (first values)
    (- (arith-ser-sum (count values))
       (reduce + values))))

(defn missing-card [& cards]
  (->> (apply map vector cards)
       (map #(apply missing-value %))
       (into [])))

(defn breaking-value [& values]
  (let [value-set (set values)
        all-values (->> values count inc range set)
        good-values (if (= (count value-set) 1)
                      (set/difference all-values value-set)
                      value-set)]
    (rand-nth (into [] good-values))))

(defn breaking-card [& cards]
  (let [num-breaks (-> cards
                       count
                       dec
                       rand-int
                       inc)
        break-indices (->> cards
                           first
                           count
                           range
                           shuffle
                           (take num-breaks)
                           set)]
    (->> (apply map vector cards)
         (map-indexed (fn [i vs] (if (break-indices i)
                                   (apply missing-value vs)
                                   (apply breaking-value vs))))
         (into []))))

(defn random-card [num-props num-values]
  (->> (partial rand-int num-values)
       repeatedly
       (take num-props)
       (into [])))

(defn pos-set-challenge [num-props num-values]
  (let [c1 (random-card num-props num-values)
        c2 (random-card num-props num-values)
        c3 (missing-card c1 c2)]
    [c1 c2 c3]))

(defn neg-set-challenge [num-props num-values]
  (let [c1 (random-card num-props num-values)
        c2 (random-card num-props num-values)
        c3 (breaking-card c1 c2)]
    [c1 c2 c3]))

(defn set-challenge [num-props num-values]
  (if (= (rand-int 2) 0)
    (pos-set-challenge num-props num-values)
    (neg-set-challenge num-props num-values)))

(defn update-challenge [state]
  (update state
          ::challenge
          (partial set-challenge (::num-props state) (::num-vals state))))

(defn update-score [state guess]
   (update state ::score (if guess
                           inc
                           #(max 0 (- % 2)))))

(defn take-guess [state guess]
  (-> state
      (update-score (= guess (apply bo? (::challenge state))))
      update-challenge))

(defn stop-game [state]
  (assoc state ::phase ::stopped))

(defn init-game []
  {::phase ::initialized})

(defn start-game [state duration]
  (-> state
      (merge {::phase ::started
              ::challenge nil
              ::score 0
              ::duration duration
              ::num-props 4
              ::num-vals 3})
      update-challenge))
