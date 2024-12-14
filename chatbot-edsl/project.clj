(defproject chatbot-edsl "0.1.0-SNAPSHOT"
  :description "Lab project for interpolation algorithms"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.cli "1.0.206"]
                 [org.clojure/test.check "1.1.1"]
                 [org.clojure/tools.logging "1.1.0"]]
  :main chatbot-edsl.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}
  :plugins [[lein-cljfmt "0.8.0"]
            [com.github.clj-kondo/lein-clj-kondo "0.2.5"]]
  :cljfmt {:indents {as-> [[:block 0]]}}
  :kondo {:lint-as :source})