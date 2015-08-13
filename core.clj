(ns teacups.core
  (:use arcadia.core
        arcadia.linear
        arcadia.hydrate)
  (:import [UnityEngine Transform]))

;; hi there

(defn- kill! [x]
  (let [s (dehydrate x)]
    (destroy x)
    s))

(def cylinder-spec
  (kill! (create-primitive :cylinder)))

(defn transform ^Transform [x]
  (get-component x Transform))

(defscn teacups
  (let [scale (v3 1 0.1 1)
        position (v3
                   0
                   (.y scale)
                   0)]
    (hydrate
      (deep-merge-mv cylinder-spec
        {:name "teacups"
         :transform [{:local-scale scale
                      :local-position position}]}))))



