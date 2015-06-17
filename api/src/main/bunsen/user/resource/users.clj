(ns bunsen.user.resource.users
  (:require [liberator.core :refer [defresource]]
            [bunsen.common.helper.resource :refer [defaults]]
            [liberator.representation :refer [ring-response]]
            [bunsen.user.model.user :as u]
            [clojure.string :as str]
            [bunsen.user.helper.tsusers :as extuser]))

(defresource users [_] defaults
  :allowed-methods #{:post :get}

  :exists? (fn [{{db :db {id :id} :route-params conn :conn remote-user :remote-user} :request}]
             (when id
               (if-let [user (u/load-user db id)]
                   (if (contains? user :user/account)
                     (let [extuser (extuser/get-ext-user (first (str/split (get user :user/account) #"@")))
                       mergeduser (extuser/merge-user user extuser)]
                       {::user mergeduser})
                     {::user user})
                   { ::user { :user/public-id id
                              :user/name "Unknown"
                              :user/email "unknown@unknown"
                              :user/roles (u/user-roles)}})))

  :handle-ok ::user

  ; validate user params
  :processable? (fn [{{db :db params :params method :request-method remote-user :remote-user} :request}]
                  (if (= :post method)
                    (if remote-user
                      {::errors "cannot edit external users"}
                      (let [errors (u/validate-user db nil params)]
                        [(empty? errors) {::errors errors}]))
                    true))

  ; respond with validation errors
  :handle-unprocessable-entity ::errors

  :post! (fn [{{conn :conn params :params} :request}]
           (when-not (some #{"admin"} (:roles params))
             (if-let [user (u/create-user! conn params)]
               {::user user})))

  ; write session cookie
  :handle-created (fn [{{params :params} :request user ::user}]
                    (let [session {:id (:user/public-id user)
                                   :roles (:user/roles user)}]
                      (ring-response {:session session}))))