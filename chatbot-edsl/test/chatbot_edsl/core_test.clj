(ns chatbot-edsl.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [chatbot-edsl.core :refer [defdialogue handle-context transition add-dialogue log-user-out log-user-in check-condition get-dialogue perform-action user-logged-in?]]))

(deftest test-add-dialogue
  (testing "Adding a dialogue"
    (add-dialogue :test "Test prompt" ["Test response" :next nil nil])
    (let [dialogue (get-dialogue :test)]
      (is (= (:id dialogue) :test))
      (is (= (:prompt dialogue) "Test prompt"))
      (is (= (count (:responses dialogue)) 1)))))

(deftest test-check-condition
  (testing "Checking condition"
    (let [context {:user-logged-in true}]
      (is (check-condition {:user-logged-in true} context))
      (is (not (check-condition {:user-logged-in false} context)))
      (is (check-condition user-logged-in? context)))))

(deftest test-perform-action
  (testing "Performing action"
    (let [context {:user-logged-in false}]
      (is (= (:user-logged-in (perform-action log-user-in context)) true))
      (is (= (:user-logged-in (perform-action log-user-out context)) false)))))

(deftest test-transition
  (testing "Transitioning to next dialogue"
    (let [response {:next-id :next}
          context {:current-dialogue :current}]
      (is (= (:current-dialogue (transition response context)) :next)))))

(deftest test-handle-context
  (testing "Handling context"
    (let [context {:current-dialogue :weather}]
      (is (= (:current-dialogue (handle-context context "Moscow")) :welcome))
      (is (= (:current-dialogue (handle-context context "Joke")) :welcome)))))

(deftest test-defdialogue
  (testing "Defining a dialogue"
    (defdialogue :test-def "Test defdialogue prompt"
      ["Test response" :next nil nil])
    (let [dialogue (get-dialogue :test-def)]
      (is (= (:id dialogue) :test-def))
      (is (= (:prompt dialogue) "Test defdialogue prompt"))
      (is (= (count (:responses dialogue)) 1))
      (is (= (:text (first (:responses dialogue))) "Test response")))))
