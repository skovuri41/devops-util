(ns tm-api.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[myapp started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[myapp has shut down successfully]=-"))})
