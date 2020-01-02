(defproject bo "0.1.0-SNAPSHOT"
  :description "A browser adaption of the card game Set"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.597"]
                 [reagent "0.9.0-rc4"]
                 [clj-commons/secretary "1.2.4"]
                 [venantius/accountant "0.2.5"]]
  :profiles
    {:dev
      {:dependencies [[com.bhauman/figwheel-main "0.2.3"]
                      [com.bhauman/rebel-readline-cljs "0.1.4"]]
       :resource-paths ["target"]
       :clean-targets ^{:protect false} ["target"]}}
  :aliases
    {"fig" ["trampoline" "run" "-m" "figwheel.main"]})
