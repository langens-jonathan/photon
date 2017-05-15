(ns photon.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [yaml.core :as yaml]
            [clj-sparql.core :as sparql]
            [clj-http.client :as client]
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
    ))

;;; TESTING

(defn perform-test-call [reduction call]
  (do
    (.println (System/out) (str "reduction: " reduction))
    (.println (System/out) (call "route"))
    (str reduction (call "route"))))

(defn perform-call [url method mutations response-code response-text]
  (let [response (client/get url)
        code (response :status)
        text (response :body)]
    (.println (System/out) response)
    (.println (System/out) (str "expecting... " response-code " ... got ... " code))
    (.println (System/out) (str "expecting... " response-text " ... got ... " text))
    (if (and (= code response-code) (= text response-text))
      (str "call with url: " url " succeedded")
      (str "[!] call with url: " url " FAILED!"))))

(defn reduce-tests [reduction test]
  (str reduction " " test))

(defn perform-test [call]
  (do 
    (.println (System/out) call)
    (let [callConfig (call (first (keys call)))
          route (callConfig "route")
          url (str "http://localhost/app" route)
          method (callConfig "method")
          mutations (callConfig "mutations")
          response (callConfig "response")
          response-code (response "code")
          response-text (response "text")]
      (reset-database)
      (perform-call url method mutations response-code response-text))))

(defn perform-tests []
  (do
    (reduce reduce-tests (map perform-test (photons "photons")))))


;;; WEB APP

(defroutes app-routes
  (GET "/" [] (perform-tests))
  (GET "/test" [] (do
                    (.println (System/out)(client/get "http://jsonplaceholder.typicode.com/albums"))
                    "TEST"))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
