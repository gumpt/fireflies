(ns fireflies-quil.core
  (:require [fireflies-quil.dynamic :as dynamic]
            [quil.core :as q]
            [quil.middleware :as m]))

(q/defsketch fireflies-quil
  :title "Fireflies at night"
  :size [dynamic/scale dynamic/scale]
  ; setup function called only once, during sketch initialization.
  :setup dynamic/setup
  ; update is called on each iteration before draw is called.
  ; It updates sketch state.
  :update dynamic/update
  :draw dynamic/draw
    ; This sketch uses functional-mode middleware.
  ; Check quil wiki for more info about middlewares and particularly
  ; fun-mode.
  :middleware [m/fun-mode])
