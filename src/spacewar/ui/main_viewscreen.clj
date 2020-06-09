(ns spacewar.ui.main-viewscreen
  (:require [quil.core :as q]
            [spacewar.ui.protocols :as p]
            [spacewar.ui.view-frame :as f]))

(deftype indicator-light [state]
  p/Drawable
  (draw [_]
    (let [{:keys [x y w h on? draw-func]} state]
      (q/stroke 0 0 0)
      (q/stroke-weight 1)
      (apply q/fill (if on? [255 255 0] [50 50 50]))
      (draw-func x y w h)))

  (setup [_] (indicator-light. (assoc state :on? false)))
  (update-state [this] this)
  (get-state [_] state))

(defn build-indicators [state]
  (let [{:keys [x y w h rows columns gap indicator-height indicator-width draw-func]} state
        cell-width (/ (- w gap gap) columns)
        cell-height (/ (- h gap gap) rows)
        cell-x-offset (- (/ cell-width 2) (/ indicator-width 2))
        cell-y-offset (- (/ cell-height 2) (/ indicator-height 2))
        indicators (for [row (range rows) column (range columns)]
                     (p/setup
                       (->indicator-light
                         {:x (+ x gap cell-x-offset (* cell-width column))
                          :y (+ y gap cell-y-offset (* cell-height row))
                          :w indicator-width
                          :h indicator-height
                          :on? false?
                          :draw-func draw-func})))]
    indicators))

(defn draw-light-panel [state]
  (let [{:keys [x y w h indicators background]} state]
    (apply q/fill background)
    (q/no-stroke)
    (q/rect x y w h)
    (doseq [indicator indicators] (p/draw indicator))))

(defn random-pattern [_ _]
  (zero? (rand-int 2)))

(defn update-light-panel [state]
  (let [{:keys [indicators on-func?]} state
        indicator-states (map p/get-state indicators)
        new-indicators (map-indexed #(->indicator-light (assoc %2 :on? (on-func? %1))) indicator-states)
        new-state (assoc state :indicators new-indicators)]
    new-state))

(defn shift-pattern [n i]
  (= i (rem (quot (q/frame-count) 3) n)))

(deftype bottom-lights [state]
  p/Drawable
  (draw [_] (draw-light-panel state))

  (setup [_]
    (let [rows 1
          columns 14
          indicators (build-indicators
                       (assoc state :rows rows
                                    :columns columns
                                    :gap 20
                                    :indicator-height 10
                                    :indicator-width 20
                                    :draw-func q/rect))
          new-state (assoc state :indicators indicators
                                 :on-func? (partial shift-pattern (* rows columns))
                                 :background [150 150 150])]
      (bottom-lights. new-state)))

  (update-state [_] (bottom-lights. (update-light-panel state))))

(deftype side-lights [state]
  p/Drawable
  (draw [_] (draw-light-panel state))

  (setup [_]
    (let [rows 10
          columns 2
          indicators (build-indicators
                       (assoc state :rows rows
                                    :columns columns
                                    :gap 20
                                    :indicator-height 15
                                    :indicator-width 15
                                    :draw-func q/ellipse))
          new-state (assoc state :indicators indicators
                                 :on-func? (partial random-pattern (* rows columns))
                                 :background [150 50 50])]
      (side-lights. new-state)))

  (update-state [this]
    (if (zero? (rand-int 15))
      (side-lights. (update-light-panel state))
      this)))

(deftype complex [state]
  p/Drawable
  (draw [_]
    (let [{:keys [frame bottom-row left-lights right-lights]} state]
      (doseq [d [frame bottom-row left-lights right-lights]] (p/draw d))))

  (setup [_]
    (let [{:keys [x y h w]} state
          left-margin 200
          right-margin 200
          bottom-margin 100
          panel-gap 50
          frame-width (- w left-margin right-margin)
          frame-height (- h bottom-margin)
          frame (p/setup
                  (f/->frame {:x (+ x left-margin)
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
                                         :w side-panel-width
                                         :h side-panel-height}))
          new-state (assoc state :frame frame
                                 :bottom-row bottom-row
                                 :left-lights left-lights
                                 :right-lights right-lights)]
      (complex. new-state)))

  (update-state [_]
    (let [elements [:frame :bottom-row :left-lights :right-lights]
          pairs (for [e elements] [e (p/update-state (e state))])
          flat-pairs (flatten pairs)]
      (complex. (->> flat-pairs (apply assoc state)))))
  )