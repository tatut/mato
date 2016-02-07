(ns mato.game
  (:require [reagent.core :as r]))

(def game-width 64)
(def game-height 32)
(def initial-length 5)
(def length-increment 5)
(def block-width 10)
(def block-height 10)
(def worm-color "lightGray")
(def food-color "red")
(def score-area-height 20)

(defn random-point []
  [(rand-int game-width)
   (rand-int game-height)])

(defn random-point-not-in [positions]
  (let [occupied (into #{} positions)]
    (first (drop-while occupied (repeatedly random-point)))))

(defn start-state []
  {:worm [[(/ game-width 2) (/ game-height 2)]]
   :length initial-length
   :direction [1 0] ;; dx dy
   :food (random-point)})

(def state (r/atom (start-state)))

(defn collision? [worm new-pos]
  (some (partial = new-pos) worm))

(defn inside-game-area? [[x y]]
  (and (<= 0 x (dec game-width))
       (<= 0 y (dec game-height))))

(defn update-game [{:keys [worm length direction food] :as state}]
  (if worm
    (let [[head-x head-y] (last worm)
          [direction-x direction-y] direction
          worm (if (= (count worm) length)
                 (vec (drop 1 worm))
                 worm)
          new-pos [(+ head-x direction-x) (+ head-y direction-y)]]

      (cond
        (= new-pos food)
        (assoc state
               :worm (conj worm new-pos)
               :length (+ length length-increment)
               :food (random-point-not-in worm))
        
        (or (collision? worm new-pos)
            (not (inside-game-area? new-pos)))
        (assoc state :worm nil)

        :default
        (assoc state :worm (conj worm
                                 new-pos))))
    state))

(defn worm
  "Render the worm SVG"
  [worm]
  [:g#worm
   (for [[x y] worm]
     ^{:key (+ (* y game-width) x)}
     [:rect {:x (* x block-width) :y (* y block-height)
             :width block-width :height block-height
             :style {:foll worm-color}}])])

(defn food [[x y]]
  [:circle {:cx (* (+ 0.5 x) block-width) :cy (* (+ 0.5 y) block-height)
            :r (/ block-width 2)
            :fill food-color}])

(defn game-over []
  (let [cx (* game-width block-width 0.5)]
    [:g 
     [:text {:x cx :y 50 :text-anchor "middle"}
      "GAME OVER!"]
     [:text {:x cx :y 130 :text-anchor "middle"}
      "press space to play again"]]))

(defn game [state]
  (let [game-width-px (* game-width block-width)
        game-height-px (* game-height block-height)]
    [:svg {:width game-width-px
           :height (+ game-height-px score-area-height)}
     (if-let [w (:worm state)]
       [:g#game
        [:rect {:x 0 :y 0 :width game-width-px :height game-height-px
                :fill "none"
                :stroke "black"}]
        [worm w]
        [food (:food state)]
        [:text {:x 10 :y (+ game-height-px score-area-height)}
         (str "SCORE: " (- (count w) initial-length))]]
       
       [game-over])]))

(defn game-main
  "Main component for the game, renders the game with the current state"
  []
  [game @state])

(defn game-update-loop
  "Continuously call update-game on each tick before render"
  []
  (swap! state update-game)
  (r/next-tick game-update-loop))

(def key-directions
  {37 [-1 0] ; left 
   38 [0 -1] ; up
   39 [1 0]  ; right
   40 [0 1]  ; down
   })

(defn ^:export start []
  (game-update-loop)
  (.addEventListener js/document.body "keydown"
                     (fn [e]
                       (if-let [d (key-directions (.-keyCode e))]
                         (swap! state assoc :direction d)
                         (if (and (nil? (:worm @state)))
                           (reset! state (start-state))))))
  (r/render [game-main]
            (.getElementById js/document "game")))
