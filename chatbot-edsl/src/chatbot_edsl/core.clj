(ns chatbot-edsl.core
  (:require [clojure.tools.logging :as log]))

(defrecord Dialogue [id prompt responses])

(def dialogues (atom {}))

(defn add-dialogue [id prompt & responses]
  (swap! dialogues assoc id (->Dialogue id prompt responses)))

(defn get-dialogue [id]
  (get @dialogues id))

(defn external-api-call [endpoint]
  (log/info "Вызов внешнего API:" endpoint)
  ;; Здесь можно добавить реальный вызов API
  )

(defn handle-context [context input]
  (log/info "Обработка контекста:" context input)
  (cond
    (= (:current-dialogue context) :weather)
    (do
      (log/info "Запрос погоды для города:" input)
      (external-api-call (str "http://api.weather.com/?city=" input))
      (println "Погода в городе" input "сейчас: ...")
      (assoc context :current-dialogue :welcome))

    (= (:current-dialogue context) :joke)
    (do
      (log/info "Запрос шутки")
      (external-api-call "http://api.joke.com/random")
      (println "Вот шутка для вас: ...")
      (assoc context :current-dialogue :welcome))

    :else
    context))

(defn transition [current-response context]
  (if-let [next-id (:next-id current-response)]
    (do
      (log/info "Переход к диалогу:" next-id)
      (assoc context :current-dialogue next-id))
    (do
      (log/info "Конец диалога.")
      nil)))

(defn check-condition [condition context]
  (cond
    (fn? condition) (condition context)
    (map? condition) (every? (fn [[k v]] (= (get context k) v)) condition)
    :else false))

(defn perform-action [action context]
  (cond
    (fn? action) (action context)
    (map? action) (reduce-kv (fn [ctx k v] (assoc ctx k v)) context action)
    :else context))

(defn handle-input [dialogue input context]
  (let [response (some #(when (and (= input (:text %))
                                   (or (not (:condition %))
                                       (check-condition (:condition %) context)))
                          %) (:responses dialogue))]
    (if response
      (let [new-context (perform-action (:action response) context)]
        (transition response new-context))
      (do
        (log/warn "Извините, я не понял ваш запрос.")
        (handle-context context input)
        context))))

(defmacro defdialogue [id prompt & responses]
  `(add-dialogue ~id ~prompt ~@(map (fn [[text next-id condition action]]
                                      {:text text :next-id next-id :condition condition :action action})
                                    responses)))

(defn user-logged-in? [context]
  (get context :user-logged-in false))

(defn log-user-in [context]
  (assoc context :user-logged-in true))

(defn log-user-out [context]
  (assoc context :user-logged-in false))

(defdialogue :welcome
  "Добро пожаловать! Как я могу вам помочь?"
  ["Расскажите о ваших услугах" :services nil nil]
  ["Как связаться с поддержкой?" :support nil nil]
  ["Узнать погоду" :weather nil nil]
  ["Расскажите анекдот" :joke nil nil]
  ["Войти в систему" :login nil log-user-in]
  ["Закончить" nil nil nil])

(defdialogue :services
  "Мы предлагаем следующие услуги: ... Что вас интересует?"
  ["Вернуться назад" :welcome nil nil]
  ["Закончить" nil nil nil])

(defdialogue :support
  "Вы можете связаться с нами по email: support@example.com."
  ["Вернуться назад" :welcome nil nil]
  ["Закончить" nil nil nil])

(defdialogue :weather
  "Пожалуйста, укажите город, для которого вы хотите узнать погоду."
  ["Вернуться назад" :welcome nil nil]
  ["Закончить" nil nil nil])

(defdialogue :joke
  "Хотите услышать анекдот?"
  ["Да" :joke-tell nil nil]
  ["Нет" :welcome nil nil]
  ["Закончить" nil nil nil])

(defdialogue :joke-tell
  "Вот анекдот для вас: ..."
  ["Вернуться назад" :welcome nil nil]
  ["Закончить" nil nil nil])

(defdialogue :login
  "Вы вошли в систему. Что дальше?"
  ["Вернуться назад" :welcome nil nil]
  ["Выйти из системы" :welcome user-logged-in? log-user-out]
  ["Закончить" nil nil nil])

(defn start-bot []
  (loop [current-dialogue (get-dialogue :welcome)
         context {:current-dialogue :welcome}]
    (when current-dialogue
      (println (:prompt current-dialogue))
      (doseq [response (:responses current-dialogue)]
        (println "-" (:text response)))
      (let [user-input (read-line)
            new-context (handle-input current-dialogue user-input context)]
        (recur (get-dialogue (:current-dialogue new-context)) new-context)))))

(defn -main []
  (println "Запуск чат-бота")
  (start-bot))
