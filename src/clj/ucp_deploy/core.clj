(ns ucp-deploy.core
  (:require
   [clojure.tools.logging :as log]
   [clojure.java.io :as io]
   [ucp-deploy.config :refer [env]]
   [org.httpkit.client :as http]
   [cli-matic.core :refer [run-cmd]]
   [mount.core :as mount])
  (:gen-class))

(defn- nexus-url
  [name version]
  (let [endpoint (get-in env [:nexus-endpoints (keyword name)])]
    (str endpoint version "/" (str name "-" version ".jar"))))

(comment
  (def nexurl https://repository.apache.org/service/local/repositories/releases/content/commons-io/commons-io/2.8.0/commons-io-2.8.0.jar)
  (http/get "http://site.com/favicon.ico" {:as :stream}
            (fn [{:keys [status headers body error opts]}]
              ;; body is a java.io.InputStream
              )))

(defn download-artifact
  [{:keys [name version]}]
  (let [artifact-name                                      (str name "-" version ".jar")
        {:keys [status headers body error opts] :as resp } @(http/get (nexus-url name version) {:as :stream})]
    (if error
      (log/info "download artifact failure " name status error)
      (io/copy body (io/file artifact-name)))))


(defn scp-artifact
  [artifact])


(defn deploy
  [artifact])

(defn -main [& args]
  (do (mount/start)
      (log/info "Started...")
      (download-artifact {:name "commons-io" :version "2.8.0"})
      (log/info "Downloaded Successfully")))
