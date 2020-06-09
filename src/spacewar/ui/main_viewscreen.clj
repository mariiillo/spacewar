(ns spacewar.ui.main-viewscreen
  (:require [quil.core :as q]
            [spacewar.ui.protocols :as p]))


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

(deftype rectangular-light [state]
  p/Drawable
  (draw [_]
    (let [{:keys [x y w h on?]} state]
      (q/stroke 0 0 0)
      (q/stroke-weight 1)
      (q/fill 255 255 0)
      (apply q/fill (if on? [255 255 0] [50 50 50]))
      (q/rect x y w h)))

  (setup [_] (rectangular-light. (assoc state :on? false)))
  (update-state [this] this)
  (get-state [_] state))

(deftype bottom-lights [state]
  p/Drawable
  (draw [_]
    (let [{:keys [x y w h indicators]} state]
      (q/fill 150 150 150)
      (q/no-stroke)
      (q/rect x y w h)
      (doseq [indicator indicators] (p/draw indicator))))

  (setup [_]
    (let [{:keys [x y w h]} state
          number 14
          gap 20
          indicator-height 10
          indicator-width 20
          spacing (/ (- w gap gap indicator-width) (dec number))
          indicator-y (+ y (/ (- h indicator-height) 2))
          indicators (map #(p/setup
                             (->rectangular-light
                               {:x (+ x gap (* spacing %))
                                :y indicator-y
                                :w indicator-width
                                :h indicator-height}))
                          (range 0 number))
          new-state (assoc state :indicators indicators)]
      (bottom-lights. new-state)))

  (update-state [_]
    (let [old-indicators (:indicators state)
          n (count old-indicators)
          on-index (rem (quot (q/frame-count) 3) n)
          indicator-states (map p/get-state old-indicators)
          indicators (map-indexed #(->rectangular-light (assoc %2 :on? (= on-index %1))) indicator-states
                                  )
          new-state (assoc state :indicators indicators)]
      (bottom-lights. new-state))))

(deftype side-lights [state]
  p/Drawable
  (draw [_]
    (let [{:keys [x y h w]} state]
      (q/no-stroke)
      (q/fill 200 50 50)
      (q/rect x y w h)))

  (setup [_] (side-lights. state))
  (update-state [this] this))

(deftype complex [state]
  p/Drawable
  (draw [_]
    (let [{:keys [frame bottom-row left-lights right-lights]} state]
      (doseq [d [frame bottom-row left-lights right-lights]] (p/draw d))))

  (setup [_]
    (let [{:keys [x y w h]} state
          left-margin 200
          right-margin 200
          bottom-margin 140
          panel-gap 50
          frame-width (- w left-margin right-margin)
          frame-height (- h bottom-margin)
          frame (p/setup
                  (->frame {:x (+ x left-margin)
                            :y y
                            :h frame-height
                            :w frame-width}))
          bottom-row-width (/ frame-width 2)
          bottom-row-left-offset (/ (- frame-width bottom-row-width) 2)
          bottom-row (p/setup
                       (->bottom-lights {:x (+ x left-margin bottom-row-left-offset)
                                         :y (+ y (- h bottom-margin) panel-gap)
                                         :h 40
                                         :w bottom-row-width}))
          side-panel-height (/ frame-height 2.5)
          side-panel-width 120
          side-panel-y (+ y (/ frame-height 5))
          left-lights (p/setup
                        (->side-lights {:x (- (+ x left-margin) panel-gap side-panel-width)
                                        :y side-panel-y
                                        :h side-panel-height
                                        :w side-panel-width}))
          right-lights (p/setup
                         (->side-lights {:x (+ x left-margin frame-width panel-gap)
                                         :y side-panel-y
                                         :h side-panel-height
                                         :w side-panel-width}))
          new-state (assoc state :frame frame
                                 :bottom-row bottom-row
                                 :left-lights left-lights
                                 :right-lights right-lights)]
      (complex. new-state)))
  (update-state [_]
    (let [elements [:frame :bottom-row :left-lights :right-lights]
          pairs (for [e elements] [e (p/update-state (e state))])
          flat-pairs (flatten pairs)]
      (complex. (->> flat-pairs (apply assoc state))))))
