(def jetty-10-version "10.0.26")
(def logback-version "1.3.16")
(def kitchensink-version "3.5.3")
(def trapperkeeper-version "4.3.0")
(def i18n-version "1.0.2")
(def slf4j-version "2.0.17")

(defproject org.openvoxproject/trapperkeeper-webserver-jetty10 "1.1.1-SNAPSHOT"
  :description "A jetty10-based webserver implementation for use with the org.openvoxproject/trapperkeeper service framework."
  :url "https://github.com/openvoxproject/trapperkeeper-webserver-jetty10"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :min-lein-version "2.9.1"

  ;; Abort when version ranges or version conflicts are detected in
  ;; dependencies. Also supports :warn to simply emit warnings.
  ;; requires lein 2.2.0+.
  :pedantic? :abort

  ;; These are to enforce consistent versions across dependencies of dependencies,
  ;; and to avoid having to define versions in multiple places. If a component
  ;; defined under :dependencies ends up causing an error due to :pedantic? :abort,
  ;; because it is a dep of a dep with a different version, move it here.
  :managed-dependencies [[org.clojure/clojure "1.12.4"]

                         [ring/ring-core "1.8.2"]
                         [ring/ring-codec "1.3.0"]
                         [commons-codec "1.20.0"]
                         [commons-io "2.20.0"]

                         [org.slf4j/slf4j-api ~slf4j-version]
                         [org.slf4j/jul-to-slf4j ~slf4j-version]
                         [org.slf4j/log4j-over-slf4j ~slf4j-version]

                         [org.bouncycastle/bcpkix-jdk18on "1.83"]
                         [org.bouncycastle/bcpkix-fips "1.0.8"]
                         [org.bouncycastle/bc-fips "1.0.2.6"]
                         [org.bouncycastle/bctls-fips "1.0.19"]
  
                         [org.openvoxproject/kitchensink ~kitchensink-version]
                         [org.openvoxproject/kitchensink ~kitchensink-version :classifier "test"]
                         [org.openvoxproject/trapperkeeper ~trapperkeeper-version]
                         [org.openvoxproject/trapperkeeper ~trapperkeeper-version :classifier "test"]]
  
  :dependencies [[org.clojure/clojure]
                 [org.clojure/java.jmx "1.1.1"]
                 [org.clojure/tools.logging "1.3.1"]

                 [org.flatland/ordered "1.5.9"]

                 [javax.servlet/javax.servlet-api "4.0.1"]
                 ;; Jetty Webserver
                 [org.eclipse.jetty/jetty-server ~jetty-10-version]
                 [org.eclipse.jetty/jetty-servlet ~jetty-10-version]
                 [org.eclipse.jetty/jetty-servlets ~jetty-10-version]
                 [org.eclipse.jetty/jetty-webapp ~jetty-10-version]
                 [org.eclipse.jetty/jetty-proxy ~jetty-10-version]
                 [org.eclipse.jetty/jetty-jmx ~jetty-10-version]
                 [org.eclipse.jetty.websocket/websocket-jetty-server ~jetty-10-version]
                 ;; used in pcp-client
                 [org.eclipse.jetty.websocket/websocket-jetty-client ~jetty-10-version]
                 [org.eclipse.jetty.websocket/websocket-jetty-api ~jetty-10-version]


                 [prismatic/schema "1.1.12"]
                 [ring/ring-servlet "1.15.3"]
                 [ring/ring-codec]
                 [ch.qos.logback/logback-access ~logback-version]
                 [ch.qos.logback/logback-core ~logback-version]
                 [ch.qos.logback/logback-classic ~logback-version]

                 [org.openvoxproject/ssl-utils "3.6.1"]
                 [org.openvoxproject/kitchensink]
                 [org.openvoxproject/trapperkeeper]
                 [org.openvoxproject/i18n ~i18n-version]
                 [org.openvoxproject/trapperkeeper-filesystem-watcher "1.3.0"]

                 [org.slf4j/jul-to-slf4j]]

  :source-paths  ["src"]
  :java-source-paths  ["java"]

  :plugins [[org.openvoxproject/i18n ~i18n-version]]

  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :username :env/CLOJARS_USERNAME
                                     :password :env/CLOJARS_PASSWORD
                                     :sign-releases false}]]

  ;; By declaring a classifier here and a corresponding profile below we'll get an additional jar
  ;; during `lein jar` that has all the code in the test/ directory. Downstream projects can then
  ;; depend on this test jar using a :classifier in their :dependencies to reuse the test utility
  ;; code that we have.
  :classifiers [["test" :testutils]]

  :test-paths ["test/clj"]

  :profiles {:shared {:source-paths ["examples/multiserver_app/src"
                                     "examples/ring_app/src"
                                     "examples/servlet_app/src/clj"
                                     "examples/war_app/src"
                                     "examples/webrouting_app/src"]
                      :java-source-paths ["examples/servlet_app/src/java"
                                          "test/java"]
                      :resource-paths ["dev-resources"]
                      :dependencies [[org.openvoxproject/http-client "2.2.1"]
                                     [org.openvoxproject/kitchensink nil :classifier "test"]
                                     [org.openvoxproject/trapperkeeper nil :classifier "test"]
                                     [org.clojure/tools.namespace "0.2.11"]
                                     [compojure "1.7.1"]
                                     [ring/ring-core]
                                     [hato "1.0.0"]]}
             :dev-only {:dependencies [[org.bouncycastle/bcpkix-jdk18on]]
                        :jvm-opts ["-Djava.util.logging.config.file=dev-resources/logging.properties"]}
             :dev [:shared :dev-only]
             :fips-only {:dependencies [[org.bouncycastle/bcpkix-fips]
                                        [org.bouncycastle/bc-fips]
                                        [org.bouncycastle/bctls-fips]]
                         ;; this only ensures that we run with the proper profiles
                         ;; during testing. This JVM opt will be set in the puppet module
                         ;; that sets up the JVM classpaths during installation.
                         :jvm-opts ~(let [version (System/getProperty "java.version")
                                          [major minor _] (clojure.string/split version #"\.")
                                          unsupported-ex (ex-info "Unsupported major Java version. Expects 17 or 21"
                                                                  {:major major
                                                                   :minor minor})]
                                      (condp = (java.lang.Integer/parseInt major)
                                        17 ["-Djava.security.properties==dev-resources/jdk17-fips-security"]
                                        21 ["-Djava.security.properties==dev-resources/jdk21-fips-security"]
                                        (throw unsupported-ex)))}
             :fips [:shared :fips-only]

             ;; per https://github.com/technomancy/leiningen/issues/1907
             ;; the provided profile is necessary for lein jar / lein install
             :provided {:dependencies [[org.bouncycastle/bcpkix-jdk18on]]
                        :resource-paths ["dev-resources"]}

             :testutils {:source-paths ^:replace ["test/clj"]
                         :java-source-paths ^:replace ["test/java"]}}

  :main puppetlabs.trapperkeeper.main)
