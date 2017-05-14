(ns photon.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [yaml.core :as yaml]
            [clj-sparql.core :as sparql]
            [clojure.java.io :as io]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]
             ]))

(def photons-yml "/photon/photon.yml")

(def files-location "/home/jonathan/Downloads/files/")

(def photons nil)

(def config {:endpoint "http://db:8890/sparql" :user "dba" :pass "dba"})

(def update-config {:endpoint "http://db:8890/sparql" :user "dba" :pass "dba"})

(defn clear-database []
  (sparql/update update-config "WITH <http://mu.semte.ch/application> DELETE {?s ?p ?o} WHERE {?s ?p ?o .}"))

(defn insert-block-in-to-database [block]
  (sparql/update update-config
                 (str
                  "WITH <http://mu.semte.ch/application> INSERT { "
                  block
                  "} WHERE {}")))

(defn reset-database []
  (do
    (clear-database)
    (insert-block-in-to-database ((photons "setup") "triple_store"))))

(defn load-photons []
  (def photons
    (yaml/from-file photons-yml)))

(defn init []
  (do
    (.println (System/out) "loading photons...")
    (load-photons)
    (.println (System/out) photons)
    ;; (reset-database)
    ))

(defn perform-tests []
  (str "all tests succeedded"))

(defroutes app-routes
  (GET "/" [] (perform-tests))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
