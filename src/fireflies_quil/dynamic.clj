(ns fireflies-quil.dynamic
  (:require [quil.core :as q]
            [clojure.reflect :as r]))

(def scale 800)
(def look 40)

(defn random-point
  []
  [(rand-int scale) (rand-int scale)])

(def points
  (repeatedly 350 random-point))

(defn create-points
  [points]
  (map (fn [[x y]] {:x x :y y}) points))

(defn create-ts
  [points]
  (let [n {}]
    (map (fn [[x y]]
           (assoc-in n [x y] (rand-int 100)))
         points)))

(defn nested-get
  [struct x y]
  (get (get struct x) y))

(defn create-phis
  [points]
  (let [n {}]
    (map (fn [[x y]]
           (assoc-in n [x y] 0))
         points)))

(defn setup []
  ; Set frame rate to 30 frames per second.
  (q/frame-rate 30)
  ; Set color mode to HSB (HSV) instead of default RGB.
  (q/color-mode :hsb)
  ; setup function returns initial state. It contains

  {:height scale :width scale
   :points (vec (create-points points))
   :ts (apply merge (create-ts points))
   :phis (apply merge (create-phis points))})

(defn increment-ts
  [points ts]
  (let [newts {}]
    (doall (map (fn [{:keys [x y]}]
                  (if-let [old-t (nested-get ts x y)]                    
                    (assoc-in newts [x y] (inc old-t))
                    (assoc-in newts [x y] 1)))
                points))))

(defn determine-phis
  [points phis ts]
  (let [newphis {}]
    (doall (map (fn [{:keys [x y]}]
                  (if-let [t (nested-get ts x y)]
                    (assoc-in newphis [x y] (->> 16 (/ Math/PI) (* t)
                                                 Math/sin Math/round))))
                points))))


(defn create-new-ts
  [x y phis ts]
  (into {} (for [a (range (- x look) (+ x look))
                 b (range (- y look) (+ y look))]
             (if (= [a b] [x y])
               {x {y (nested-get ts x y)}}
               (if-let [t (nested-get ts a b)]
                 (if (not= 1 (nested-get phis a b))
                   (if (= nil t)
                     {a {b 0}}
                     {a {b (inc t)}})
                   {a {b t}}))))))

(defn hit-neighbors-if-firing
  [{:keys [x y] :as point} phis ts]
  (if (= 1 (nested-get phis x y))
    (create-new-ts x y phis ts)
    {x {y (nested-get ts x y)}}))

(defn update-neighbors
  [points phis ts]
  (map #(hit-neighbors-if-firing % phis ts) points))

(defn update [{:keys [points phis ts] :as state}]
  (let [intermediate-ts (into {} (increment-ts points ts))
        intermediate-phis (into {} (determine-phis points phis
                                                   intermediate-ts))
        final-ts (into {} (update-neighbors points intermediate-phis
                                            intermediate-ts))]
    (as-> state s
          (assoc s :ts final-ts)
;          (assoc s :ts intermediate-ts)
          (assoc s :phis (into {}
                               (determine-phis points intermediate-phis (:ts s)))))))

(defn draw [state]
   ; Clear the sketch by filling it with light-grey color.
  (q/background 50)

  ; Print fireflies
  (dorun (map (fn [{:keys [x y]}]
                (if (= 1 (get (get (:phis state) x) y))
                  (q/fill 55 76 100)
                  (q/fill 38 52 10))
                (q/rect (- x 5) (- y 5) 5 5))
              (:points state))))
