(ns ucp-deploy.core
  (:require
   [clojure.tools.logging :as log]
   [clojure.java.io :as io]
   [clj-ssh.ssh :as ssh]
   [ucp-deploy.config :refer [env]]
   [org.httpkit.client :as http]
   [cuerdas.core :as str]
   [cli-matic.core :refer [run-cmd]]
   [mount.core :as mount])
  (:gen-class))

(defn- nexus-url
  [base name version type]
  (str base version "/" (str name "-" version "." type)))

(defn download-artifact
  [url insecure?]
  (let [_                                                 (log/info url)
        {:keys [status headers body error opts] :as resp} @(http/get url {:insecure? insecure?
                                                                          :as        :stream})]
    (if error
      (log/info "download artifact failure " status error)
      (io/copy body (io/file (str/slice url (inc (str/last-index-of url "/"))))))))

(defn scp-artifact
  [{:keys [name version type hostname username password remote-path]}]
  (try
    (let [artifact-name (str name "-" version "." type)
          _             (log/info "sftp artifact: " artifact-name " to host " hostname)
          agent         (ssh/ssh-agent {})
          session       (ssh/session agent hostname {:username                 username
                                                     :password                 password
                                                     :strict-host-key-checking :no})]
      (ssh/with-connection session
        (let [channel (ssh/ssh-sftp session)]
          (ssh/with-channel-connection channel
            (ssh/sftp channel {} :cd remote-path)
            (ssh/sftp channel {} :put artifact-name artifact-name)))
        (ssh/ssh session {:cmd (str "chmod 755 " remote-path "/" artifact-name)})
        (let [result (ssh/ssh session {:cmd (str "du -B 1 " remote-path "/" artifact-name)})]
          (log/info (result :out)))))
    (catch Exception e (str "caught exception: " (.getMessage e)))))

(comment

  (let [{:keys [base name version type] :as params} (get env :commons-io)
        url       (nexus-url base name version type)
        insecure? (get env :insecure)]
    (download-artifact url insecure?))

  (let [scp-args {:hostname    (get env :ssh-hostname)
                  :username    (get env :ssh-username)
                  :password    (get env :ssh-password)
                  :remote-path (get env :remote-path)}
        artifact-args (get env :commons-io)]
    (scp-artifact (merge scp-args artifact-args)))

  (let [{:keys [base name version type] :as artifact-args} (get env :commons-io)
        url                                                (nexus-url base name version type)
        insecure?                                          (get env :insecure)
        scp-args                                           {:hostname    (get env :ssh-hostname)
                                                            :username    (get env :ssh-username)
                                                            :password    (get env :ssh-password)
                                                            :remote-path (get env :remote-path)}]
    (download-artifact url insecure?)
    (scp-artifact (merge scp-args artifact-args)))

  )

(defn deploy
  [artifact])

(defn -main [& args]
  (mount/start)
  (log/info "Started...")
  (let [{:keys [base name version type] :as artifact-args} (get env :commons-io)
        url                                                (nexus-url base name version type)
        insecure?                                          (get env :insecure)
        scp-args                                           {:hostname    (get env :ssh-hostname)
                                                            :username    (get env :ssh-username)
                                                            :password    (get env :ssh-password)
                                                            :remote-path (get env :remote-path)}]
    (download-artifact url insecure?)
    (scp-artifact (merge scp-args artifact-args)))
  (log/info "Completed..."))
