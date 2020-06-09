(ns spacewar.ui.view-frame
  (:require [quil.core :as q]
            [spacewar.ui.protocols :as p]))

(defn star-count [_])
(defn star-size [_])
(defn star-in-frame [_ _ _])
(defn star-color [_])
(defn move-stars [_])
(defn make-stars [_])
(defn add-stars [_])

(deftype star-field [state]
  p/Drawable
  (draw [_]
    (let [{:keys [x y h w stars]} state]
      (q/no-stroke)
      (doseq [star stars]
        (let [{:keys [h-distance v-distance luminosity angle]} star
              v (* h (/ v-distance h-distance))
              vx (* v (Math/cos angle))
              vy (* v (Math/sin angle))
              sx (+ vx x (/ w 2))
              sy (+ vy y (/ h 2))
              m (/ luminosity
                   (Math/sqrt (+ (* h-distance h-distance) (* v-distance v-distance))))
              sz (star-size m)]
          (when (star-in-frame state sx sy)
            (do
              (apply q/fill (star-color m))
              (q/ellipse sx sy sz sz)))))))
  (setup [_] (star-field. (assoc state :stars (make-stars star-count))))
  (update-state [_] (star-field. (add-stars (update state :stars move-stars)))))

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
