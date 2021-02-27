(ns user
  (:require [ucp-deploy.config :refer [env]]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [mount.core :as mount]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(defn start []
  (mount/start))

(defn stop []
  (mount/stop))

(defn restart []
  (stop)
  (start))
