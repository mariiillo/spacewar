(ns spacewar.ui.view-frame
  (:require [quil.core :as q]
            [spacewar.ui.protocols :as p]))

(deftype star-field [state]
  p/Drawable
  (draw [this] this)
  (setup [this] this)
  (update-state [this] this))

(deftype frame [state]
  p/Drawable
  (draw [_]
    (let [{:keys [x y w h contents]} state]
      (q/stroke 0 0 255)
      (q/stroke-weight 5)
      (q/fill 0 0 0)
      (q/rect x y w h 5)
      (p/draw contents)))
  (setup [_]
    (let [{:keys [x y w h]} state
          contents (p/setup (->star-field {:x x :y y :h h :w w}))]
      (frame. (assoc state :contents contents))))
  (update-state [_] (frame. (assoc state :contents (p/update-state (:contents state))))))
