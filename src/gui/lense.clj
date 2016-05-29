(ns gui.lense
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [gui.actors :refer [graph-actors]]
            [gui.gui :refer [graph-gui]]
            [gui.hnav :as hnav]
            [seesaw.core :refer :all]
            [gui.globals :refer :all]
            [narjure.core :as nar]
            [narjure.general-inference.concept-selector :as concept-selector]
            [narjure.general-inference.event-selector :as event-selector]
            [narjure.general-inference.general-inferencer :as general-inferencer]
            [narjure.memory-management.concept-manager :as concept-manager]
            [narjure.memory-management.event-buffer :as event-buffer]
            [narjure.memory-management.task-dispatcher :as task-dispatcher]
            [narjure.perception-action.operator-executor :as operator-executor]
            [narjure.perception-action.sentence-parser :as sentence-parser]
            [narjure.perception-action.task-creator :as task-creator]))

(def debugmessage {:concept-selector concept-selector/display
                   :event-selector event-selector/display
                   :general-inferencer general-inferencer/display
                   :concept-manager concept-manager/display
                   :event-buffer event-buffer/display
                   :task-dispatcher task-dispatcher/display
                   :operator-executor operator-executor/display
                   :sentence-parser sentence-parser/display
                   :task-creator task-creator/display})

(def debugmessage :empty)

(def graphs [[graph-actors] [graph-gui]])

(defn setup []
  (q/frame-rate 30)
  ;(nar/run)
  (merge hnav/states {}))

(defn update [state] state)

(defn nameof [a]
  (if (string? a) a (name a)))

(defn draw-actor [{:keys [name px py backcolor frontcolor]} node-width node-height]
  (apply q/fill (if (= backcolor nil) [255 255 255] backcolor))
  (q/rect px py node-width node-height)
  (apply q/fill (if (= frontcolor nil) [0 0 0] frontcolor))
  (q/text (str (nameof name) "\n" (name debugmessage)) (+ px 5) (+ py 10)))

(defn draw-graph [[nodes vertices node-width node-height]]
  (doseq [c vertices]
    (let [left (first (filter #(= (:from c) (:name %)) nodes))
          right (first (filter #(= (:to c) (:name %)) nodes))
          pxtransform (fn [x] (+ (:px x) (/ node-width 2.0)))
          pytransform (fn [y] (+ (:py y) (/ node-height 2.0)))]
      (q/line (pxtransform left) (pytransform left)
              (pxtransform right) (pytransform right))))
  (doseq [a nodes]
    (draw-actor a node-width node-height)))

(defn draw [state]
  (q/background 255)
  (q/reset-matrix)
  (hnav/transform state)
  (doseq [[g] graphs]
    (draw-graph g))
  (q/text @input-string 400 -350))

(defn key-pressed [state event]
  (let [name (name (:key event))
        code (:key-code event)]
    (swap! input-string (fn [inputstr] (str (if (not (= code 8))
                                              inputstr "")
                                     (if (not (= name "shift"))
                                       (if (not (= code 8))
                                         name "") ""))))
    state))

(q/defsketch example
             :size [(hnav/width) (hnav/height)]
             :setup setup
             :draw draw
             :update update
             :mouse-pressed (partial hnav/mouse-pressed graphs)
             :mouse-dragged hnav/mouse-dragged
             :mouse-wheel hnav/mouse-wheel
             :key-pressed key-pressed
             :middleware [m/fun-mode]
             :features [ :resizable ]
             :title "OpenNARS 2.0.0: Lense")