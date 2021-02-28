(require '[clojure.java.io :as io])
(defproject ucp-deploy "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.2"]
                 [cheshire "5.10.0"]
                 [clojure.java-time "0.3.2"]
                 [me.raynes/fs "1.4.6"]
                 [cprop "0.1.17"]
                 [nrepl "0.8.3"]
                 [mount "0.1.16"]
                 [funcool/cuerdas "2020.03.26-3"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [org.clojure/tools.cli "1.0.194"]
                 [cli-matic "0.4.3"]
                 [org.clojure/tools.logging "1.1.0"]
                 [semantic-csv "0.2.1-alpha1"]
                 [http-kit "2.5.3"]
                 [medley "1.3.0"]
                 [clj-commons/clj-ssh "0.5.15"]
                 [metosin/ring-http-response "0.9.2"]]

  :min-lein-version "2.0.0"

  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :resource-paths ["resources"]
  :target-path "target/%s/"
  :main ucp-deploy.core
  :bin {:name          "ucpdeploy"
        :bin-path      "./"
        :bootclasspath false
        ;; :jvm-opts      ["-server" "-Dfile.encoding=utf-8" "$JVM_OPTS" ]
        }
  :plugins [[lein-binplus "0.6.6"]
            [lein-eftest "0.5.1"]
            [lein-kibit "0.1.6"]
            [lein-libdir "0.1.1"]]
  :profiles
  {:uberjar {:omit-source              true
             :aot                      :all
             :uberjar-name             "ucp-deploy.jar"
             :source-paths             ["env/prod/clj"]
             :resource-paths           ["env/prod/resources"]
             :keep-non-project-classes true}

   :dev  [:project/dev :profiles/dev]
   :test [:project/dev :project/test :profiles/test]

   :project/dev   {:jvm-opts       ["-Dconf=dev-config.edn"]
                   :dependencies   [[expound "0.8.9"]
                                    [pjstadig/humane-test-output "0.10.0"]
                                    [prone "2020-01-17"]
                                    [lein-binplus "0.6.6"]]
                   :plugins        [[com.jakemccrary/lein-test-refresh "0.23.0"]]
                   :source-paths   ["env/dev/clj"]
                   :resource-paths ["env/dev/resources"]
                   :repl-options   {:init-ns user}
                   :injections     [(require 'pjstadig.humane-test-output)
                                    (pjstadig.humane-test-output/activate!)]}
   :project/test  {:jvm-opts       ["-Dconf=test-config.edn"]
                   :resource-paths ["env/test/resources"]}
   :profiles/dev  {}
   :profiles/test {}})
