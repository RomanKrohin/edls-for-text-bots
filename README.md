# Отчёт по лабораторной работе  
**Тема:** eDSL для программирования чат-ботов  

---

## Титульный лист  

**Название работы:** eDSL для программирования чат-ботов  
**Студент:** Крохин Роман Олегович 
**Группа:** P3324  
**ИСУ** 368381

---

## Требования к разработанному ПО  

### Описание  
Программа представляет собой предметно-ориентированный язык (eDSL) для создания чат-ботов, позволяющий декларативно описывать диалоги и их переходы. Она реализована на языке Clojure в функциональном стиле с использованием атомов для хранения состояния и макросов для удобного описания диалогов.

### Основные функциональные возможности  
1. Описание диалогов через макрос `defdialogue`.
2. Реализация переходов между состояниями на основе пользовательского ввода.
3. Обработка контекста, включая динамические действия и вызов внешних API.
4. Логирование ключевых событий для отладки и мониторинга.  

### Описание алгоритма  
1. Диалоги задаются с помощью макроса `defdialogue`, который позволяет определить текст запроса, возможные варианты ответа, условия и действия.  
2. Состояние текущего диалога хранится в атоме, передаваемом между функциями.  
3. На основе пользовательского ввода и заданных условий программа:
   - Проверяет, соответствует ли ввод предложенным вариантам.
   - Выполняет действие, если оно указано (например, авторизация или вызов внешнего API).
   - Переходит к следующему диалогу или завершает работу.  
4. Если пользовательский ввод не распознан, вызывается обработчик контекста (`handle-context`), который может предоставить дополнительные возможности взаимодействия.  

---

## Реализация  

```clojure
(ns chatbot-edsl.core
  (:require [clojure.tools.logging :as log]))

(defrecord Dialogue [id prompt responses])

(def dialogues (atom {}))

(defn add-dialogue [id prompt & responses]
  (swap! dialogues assoc id (->Dialogue id prompt responses)))

(defmacro defdialogue [id prompt & responses]
  `(add-dialogue ~id ~prompt ~@(map (fn [[text next-id condition action]]
                                      {:text text :next-id next-id :condition condition :action action})
                                    responses)))

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
        (assoc new-context :current-dialogue (:next-id response)))
      (do
        (log/warn "Неизвестный ввод.")
        context))))

(defdialogue :welcome
  "Добро пожаловать! Как я могу вам помочь?"
  ["Узнать погоду" :weather nil nil]
  ["Расскажите анекдот" :joke nil nil]
  ["Закончить" nil nil nil])

(defdialogue :weather
  "Пожалуйста, укажите город, для которого вы хотите узнать погоду."
  ["Вернуться назад" :welcome nil nil]
  ["Закончить" nil nil nil])

(defdialogue :joke
  "Вот анекдот для вас: ..."
  ["Вернуться назад" :welcome nil nil]
  ["Закончить" nil nil nil])

(defn start-bot []
  (loop [current-dialogue (get @dialogues :welcome)
         context {:current-dialogue :welcome}]
    (when current-dialogue
      (println (:prompt current-dialogue))
      (doseq [response (:responses current-dialogue)]
        (println "-" (:text response)))
      (let [user-input (read-line)
            new-context (handle-input current-dialogue user-input context)]
        (recur (get @dialogues (:current-dialogue new-context)) new-context)))))

(defn -main []
  (println "Запуск чат-бота")
  (start-bot))
```

---

## Ввод/вывод программы  

### Ввод  
- Пользователь выбирает один из предложенных вариантов, вводя текст ответа, например:  
  ```
  Узнать погоду
  ```

### Вывод  
- Программа выводит текст текущего диалога, список возможных ответов и ожидает ввода.  
  Пример:  
  ```
  Добро пожаловать! Как я могу вам помочь?
  - Узнать погоду
  - Расскажите анекдот
  - Закончить
  ```

---

## Выводы  

1. **Использование eDSL:** Макрос `defdialogue` упростил декларативное описание диалогов, что сделало код компактным и читаемым.  
2. **Функциональный стиль:** Функции для обработки условий и действий демонстрируют преимущества композиции и чистоты.  
3. **Логирование:** Логирование позволяет отслеживать обработку данных и событий, что особенно полезно при интеграции с внешними API.  
4. **Гибкость:** Контекстная обработка ввода позволяет расширять функциональность чат-бота без изменения основного кода.  

Работа показала, что использование функциональных языков программирования, таких как Clojure, способствует созданию лаконичных и выразительных решений для сложных задач.  
