(ns spacewar.ui.main-viewscreen
  (:require [quil.core :as q]
            [spacewar.ui.protocols :as p]))

;(deftype indicator-light [state]
;  p/Drawable
;  (draw [{:keys [x y w h on? draw-func]} state]
;    (q/stroke 0 0 0)
;    (q/stroke-weight 1)
;    (apply q/fill (if on? [255 255 0] [50 50 50]))
;    (draw-func x y w h))
;  (setup [_] (indicator-light. (assoc state :on? false)))
;  (update-state [this] this)
;  (get-state [_] state))
;
;(defn draw-light-panel [state]
;  (let [{:keys [x y w h indicators background]} state]
;    (apply q/fill background)
;    (q/no-stroke)
;    (q/rect x y w h)
;    (doseq [indicator indicators] (p/draw indicator))))
;
;(defn update-light-panel [state]
;  (let [{:keys [indicators on-func?]} state
;        indicator-states (map p/get-state indicators)
;        new-indicators (map-indexed #(->indicator-light (assoc %2 :on? (on-func? %1))) indicator-states)
;        new-state (assoc state :indicators new-indicators)]
;    new-state))

(deftype frame [state]
  p/Drawable
  (draw [_]
    (let [{:keys [x y w h]} state]
      (q/background 200 200 200)
      (q/stroke 0 0 255)
      (q/stroke-weight 5)
      (q/fill 0 0 0)
      (q/rect x y w h 5)))

  (setup [_] (frame. state))
  (update-state [_] (frame. state)))

(deftype bottom-lights [state]
  p/Drawable
  (draw [_]
    (let [{:keys [x y w h]} state]
      (q/fill 150 150 150)
      (q/no-stroke)
      (q/rect x y w h)))

  (setup [_] (bottom-lights. state))
  (update-state [_] (bottom-lights. state)))

(deftype side-lights [state]
  p/Drawable
  (draw [_]
    (let [{:keys [x y h w]} state]
      (q/no-stroke)
      (q/fill 200 50 50)
      (q/rect x y w h)))

  (setup [_] (side-lights. state))
  (update-state [_] (side-lights. state)))

(deftype complex [state]
  p/Drawable
  (draw [_]
    (p/draw (:frame state))
    (p/draw (:bottom-row state))
    (p/draw (:left-lights state))
    (p/draw (:right-lights state)))

  (setup [_]
    (let [{:keys [x y w h]} state
          left-margin 200
          right-margin 200
          bottom-margin 140
          panel-gap 50
          frame-width (- w left-margin right-margin)
          frame-height (- h bottom-margin)
          frame (->frame {:x (+ x left-margin)
                          :y y
                          :h frame-height
                          :w frame-width})
          bottom-row-width (/ frame-width 2)
          bottom-row-left-offset (/ (- frame-width bottom-row-width) 2)
          bottom-row (->bottom-lights {:x (+ x left-margin bottom-row-left-offset)
                                       :y (+ y (- h bottom-margin) panel-gap)
                                       :h 40
                                       :w bottom-row-width})
          side-panel-height (/ frame-height 2.5)
          side-panel-width 120
          side-panel-y (+ y (/ frame-height 5))
          left-lights (->side-lights {:x (- (+ x left-margin) panel-gap side-panel-width)
                                      :y side-panel-y
                                      :h side-panel-height
                                      :w side-panel-width})
          right-lights (->side-lights {:x (+ x left-margin frame-width panel-gap)
                                      :y side-panel-y
                                      :h side-panel-height
                                      :w side-panel-width})
          new-state (assoc state :frame frame
                                 :bottom-row bottom-row
                                 :left-lights left-lights
                                 :right-lights right-lights)]
      (complex. new-state)))
  (update-state [_] (complex. state)))
