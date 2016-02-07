(defproject mato "0.1.0-SNAPSHOT"
  :description "Minimalist worm game"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [figwheel "0.5.0-6"]
                 [reagent "0.6.0-alpha"]]
  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-figwheel "0.5.0-6"]
            [cider/cider-nrepl "0.10.2"]]

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src"]
                        :figwheel {:on-jsload "mato.game/reload"}
                        :compiler {:optimizations :none
                                   :source-map true
                                   :output-to "resources/public/js/compiled/mato.js"
                                   :output-dir "resources/public/js/compiled/out"}}

                       {:id "prod"
                        :source-paths ["src"]
                        :compiler {:optimizations :advanced
                                   :output-to "mato.js"}}
                       ]}
)


