(ns teacups.core
  (:use arcadia.core
        arcadia.linear
        arcadia.hydrate)
  (:require [arcadia.updater :as updr])
  (:import [UnityEngine Transform GameObject Component PhysicMaterial Rigidbody MeshCollider BoxCollider MeshRenderer Color]))

;; (import '[UnityEngine
;;             Color
;;             PhysicMaterial
;;             Transform
;;             Rigidbody
;;             Time]
;;     '[UnityEditor
;;       Selection])

;; hi there

(defmacro set-with! [obj [sym & props] & body]
  `(let [obj# ~obj
         ~sym (.. obj# ~@props)]
     (set! (.. obj# ~@props) (do ~@body))))

(def ^:dynamic *you*
  ;;(object-named "NormalController")
  (object-named "OVRPlayerController")
  )



(defn- kill! [x]
  (let [s (dehydrate x)]
    (destroy x)
    s))

(def cylinder-spec
  (let [s (kill! (create-primitive :cylinder))]
    (-> s
      (dissoc :capsule-collider)
      (assoc :mesh-collider [{:shared-mesh (:shared-mesh (first (:mesh-filter s)))}]))))

(defn transform ^Transform [x]
  (get-component x Transform))

(defn game-object ^GameObject [x]
  (condcast-> x x
    GameObject x
    Component (.gameObject x)))

(defn shader-material ^Material [^String name]
  (Material. (Shader/Find name)))

(def white
  (shader-material "Standard"))
  
(def black
  (doto (shader-material "Standard")
    (set-with! [_ color]
      (Color. 0 0 0))))

(defn tea-cupit [subcirc-n, diameter]
  (for [i (range subcirc-n)
        :let [height 1
              lp (point-pivot
                   (v3 0.5 (+ height 1) 0)
                   (v3 0)
                   (aa (* i (/ 360 subcirc-n))
                     0 1 0))]]
    (->
      (deep-merge-mv cylinder-spec
        {:name (str "subcirc" i)
         :transform [{:local-position lp
                      :local-scale [diameter height diameter]}]})
      (assoc :mesh-renderer [{:shared-material (if (odd? i)
                                                 white
                                                 black)}]))))


(defscn teacups
  (let [base-diameter 10
        base-scale (v3 base-diameter 0.1 base-diameter)
        base-position (v3
                        0
                        (+ 0.5 (/ (.y base-scale) 2))
                        20)
        subcirc-n 3
        subcircs (last
                   (take 1
                     (iterate
                       (fn [kids]
                         (for [subc (tea-cupit subcirc-n 0.59)]
                           (-> subc
                             (assoc :children kids)
                             (dissoc :mesh-collider)
                             (update-in [:transform 0 :local-position]
                               #(v3+ % (v3 0 0 0))))))
                       (for [subc (tea-cupit subcirc-n 0.59)]
                         (assoc subc :children
                           (vec (let [n 3]
                                  (tea-cupit n (/ n (+ 1 (* 2 n)))))))))))]
    (hydrate
      (deep-merge-mv (dissoc cylinder-spec :mesh-collider)
        {:name "teacups"
         :transform [{:local-scale base-scale
                      :local-position base-position}]
         :children (vec subcircs)}))))

(defn position-you! [v]
  (set!
    (.position (transform *you*))
    v))

(defn teacup-driver []
  (doto (transform teacups)
    (set-with! [r localRotation]
      (qq* r (aa 0.2 0 1 0))))
  (let [circs-1 (map game-object (transform teacups))]
    (doseq [circ circs-1]
      (let [t (transform circ)]
        (doto t
          (set-with! [r localRotation]
            (qq* r (aa -1.5 0 1 0)))))
      (doseq [trns (transform circ)]
        (set-with! trns [r localRotation]
          (qq* r (aa 1 0 1 0))))))
  (let [your-teacup (game-object (first (first (transform teacups))))]
    (position-you!
      (v3+ 
        (.position (transform your-teacup))
        (v3 0 2 0)))
    ;; (set-with! (transform *you*) [r localRotation]
    ;;   (qq* ))
    )
  )

(when-not *compile-files*
  (updr/put! :teacup-driver #'teacup-driver))

(updr/toggle! :teacup-driver)







