
(set-env!
 :dependencies '[[org.clojure/clojurescript "1.8.40"      :scope "test"]
                 [org.clojure/clojure       "1.8.0"       :scope "test"]
                 [adzerk/boot-cljs          "1.7.170-3"   :scope "test"]
                 [adzerk/boot-reload        "0.4.6"       :scope "test"]
                 [cirru/boot-cirru-sepal    "0.1.6"       :scope "test"]
                 [org.clojure/core.async    "0.2.374"]
                 [mvc-works/hsl             "0.1.2"]
                 [mvc-works/respo           "0.1.22"]
                 [mvc-works/respo-spa       "0.1.4"]])

(require '[adzerk.boot-cljs   :refer [cljs]]
         '[adzerk.boot-reload :refer [reload]]
         '[cirru-sepal.core   :refer [transform-cirru]]
         '[respo.alias        :refer [html head title script style meta' div link body]]
         '[respo.render.static-html :refer [make-html]]
         '[clojure.java.io    :as    io])

(def +version+ "0.1.0")

(task-options!
  pom {:project     'memkits/timedrops
       :version     +version+
       :description "Workflow"
       :url         "https://github.com/Memkits/timedrops"
       :scm         {:url "https://github.com/Memkits/timedrops"}
       :license     {"MIT" "http://opensource.org/licenses/mit-license.php"}})

(deftask compile-cirru []
  (set-env!
    :source-paths #{"cirru/"})
  (comp
    (transform-cirru)
    (target :dir #{"compiled/"})))

(defn use-text [x] {:attrs {:innerHTML x}})
(defn html-dsl [data fileset]
  (make-html
    (html {}
    (head {}
      (title (use-text "Timedrops"))
      (link {:attrs {:rel "icon" :type "image/png" :href "drop-192x192.png"}})
      (link (:attrs {:rel "manifest" :href "manifest.json"}))
      (meta' {:attrs {:charset "utf-8"}})
      (meta' {:attrs {:name "viewport" :content "width=device-width, initial-scale=1"}})
      (link {:attrs {:rel "manifest" :href "manifest.json"}})
      (style (use-text "body {margin: 0;}"))
      (style (use-text "body * {box-sizing: border-box;}"))
      (script {:attrs {:id "config" :type "text/edn" :innerHTML (pr-str data)}}))
    (body {}
      (div {:attrs {:id "app"}})
      (script {:attrs {:src "main.js"}})))))

(deftask html-file
  "task to generate HTML file"
  [d data VAL edn "data piece for rendering"]
  (with-pre-wrap fileset
    (let [tmp (tmp-dir!)
          out (io/file tmp "index.html")]
      (empty-dir! tmp)
      (spit out (html-dsl data fileset))
      (-> fileset
        (add-resource tmp)
        (commit!)))))

(deftask dev []
  (set-env!
    :asset-paths #{"assets"}
    :source-paths #{"cirru/src"})
  (comp
    (html-file :data {:build? false})
    (watch)
    (transform-cirru)
    (reload :on-jsload 'timedrops.core/on-jsload)
    (cljs)
    (target)))

(deftask build-simple []
  (set-env!
    :asset-paths #{"assets"}
    :source-paths #{"cirru/src"})
  (comp
    (transform-cirru)
    (cljs :optimizations :simple)
    (html-file :data {:build? false})
    (target)))

(deftask build-advanced []
  (set-env!
    :asset-paths #{"assets"}
    :source-paths #{"cirru/src"})
  (comp
    (transform-cirru)
    (cljs :optimizations :advanced)
    (html-file :data {:build? true})
    (target)))

(deftask rsync []
  (fn [next-task]
    (fn [fileset]
      (sh "rsync" "-r" "target/" "frp.im:repo/Memkits/timedrops" "--exclude" "main.out" "--delete")
      (next-task fileset))))

(deftask send-tiye []
  (comp
    (build-advanced)
    (rsync)))

(deftask build []
  (set-env!
    :source-paths #{"cirru/src"})
  (comp
    (transform-cirru)
    (pom)
    (jar)
    (install)
    (target)))

(deftask deploy []
  (set-env!
    :repositories #(conj % ["clojars" {:url "https://clojars.org/repo/"}]))
  (comp
    (build)
    (push :repo "clojars" :gpg-sign (not (.endsWith +version+ "-SNAPSHOT")))))
