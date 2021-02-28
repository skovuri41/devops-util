(ns ucp-deploy.core
  (:require
   [clojure.tools.logging :as log]
   [clojure.java.io :as io]
   [clj-ssh.ssh :as ssh]
   [ucp-deploy.config :refer [env]]
   [org.httpkit.client :as http]
   [cli-matic.core :refer [run-cmd]]
   [mount.core :as mount])
  (:gen-class))

(defn- nexus-url
  [name version]
  (let [endpoint (get-in env [:nexus-endpoints (keyword name)])]
    (str endpoint version "/" (str name "-" version ".jar"))))

(defn download-artifact
  [{:keys [name version]}]
  (let [artifact-name                                      (str name "-" version ".jar")
        endpoint                                           (nexus-url name version)
        _                                                  (log/info endpoint)
        {:keys [status headers body error opts] :as resp } @(http/get endpoint {:as :stream})]
    (if error
      (log/info "download artifact failure " name status error)
      (io/copy body (io/file artifact-name)))))

(defn scp-artifact
  [{:keys [name version]}]
  (let [artifact-name (str name "-" version ".jar")
        hostname      (get env :ssh-hostname)
        username      (get env :ssh-username)
        password      (get env :ssh-password)
        remote-path   "/tmp"
        _             (log/info "sftp artifact: " artifact-name " to host " hostname)
        agent (ssh/ssh-agent {})
        session (ssh/session agent hostname {:username                 username
                                             :password                 password
                                             :strict-host-key-checking :no})]
    (ssh/with-connection session
      (let [channel (ssh/ssh-sftp session)]
        (ssh/with-channel-connection channel
          (ssh/sftp channel {} :cd remote-path)
          (ssh/sftp channel {} :put artifact-name artifact-name)))
      (ssh/ssh session {:cmd (str "chmod 755 " remote-path "/" artifact-name)})
      (let [result (ssh/ssh session {:cmd (str "du -B 1 " remote-path "/" artifact-name)})]
        (log/info (result :out))))))

(comment
  (scp-artifact {:name "commons-io" :version "2.8.0"}))

(defn deploy
  [artifact])

(defn -main [& args]
  (mount/start)
  (log/info "Started...")
  (download-artifact {:name "commons-io" :version "2.8.0"})
  (scp-artifact {:name "commons-io" :version "2.8.0"})
  (log/info "Completed..."))
