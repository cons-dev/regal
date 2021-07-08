(ns lambdaisland.regal.test-util
  (:require [lambdaisland.regal :as regal])
  #?(:cljs (:require-macros [lambdaisland.regal.test-util :refer [inline-resource]])
     :clj (:require [clojure.java.io :as io])))

#?(:clj
   (defmacro inline-resource [resource-path]
     (read-string (slurp (io/resource resource-path)))))

(defn read-test-cases []
  #? (:clj (read-string (slurp (io/resource "lambdaisland/regal/test_cases.edn")))
      :cljs (inline-resource "lambdaisland/regal/test_cases.edn")))

(defn flavor-parents [flavor]
  (->> flavor
       (iterate (comp first (partial parents regal/flavor-hierarchy)))
       (take-while identity)))

(defn format-cases [cases]
  (for [[form pattern & tests :as case] cases
        :let [[props tests] (if (map? (first tests))
                              [(first tests) (rest tests)]
                              [{} tests])]]
    (with-meta (merge
                {:pattern   pattern
                 :form      form
                 :tests     tests}
                props)
      (meta case))))

(defn test-cases
  ([]
   (let [cases (read-test-cases)]
     (loop [[id & cases] cases
            result []]
       (if id
         (recur (drop-while vector? cases)
                (conj result
                      {:id id
                       :cases (format-cases (take-while vector? cases))}))
         result)))))

#?(:clj
   (do
     (defn re2-compile ^com.google.re2j.Pattern [s]
       (com.google.re2j.Pattern/compile s))
     (defn re2-groups
       [^com.google.re2j.Matcher m]
       (let [gc  (. m (groupCount))]
         (if (zero? gc)
           (. m (group))
           (loop [ret [] c 0]
             (if (<= c gc)
               (recur (conj ret (. m (group c))) (inc c))
               ret)))))
     (defn re2-find
       ([^com.google.re2j.Matcher m]
        (when (. m (find))
          (re2-groups m)))
       ([^com.google.re2j.Pattern re s]
        (let [m (.matcher re s)]
          (re2-find m))))))

#_
(test-cases)
