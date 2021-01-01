//
// Created by cherit on 12/27/20.
//

#include "list_head.h"

UserListHead *create_list_head() {
    UserListHead *list_head = (UserListHead *) malloc(sizeof(UserListHead));
    list_head->list = NULL;
    list_head->mutex = (pthread_mutex_t) PTHREAD_MUTEX_INITIALIZER;
    return list_head;
}

void add_to_list(UserListHead *list, User *user) {
    pthread_mutex_lock(&list->mutex);
    list->list = add_to_usr_list(list->list, user);
    pthread_mutex_unlock(&list->mutex);
}

User *find_on_list(UserListHead *list, char *username) {
    pthread_mutex_lock(&list->mutex);
    User *user = find_on_usr_list(list->list, username);
    pthread_mutex_unlock(&list->mutex);
    return user;
}

void remove_user_from_list(UserListHead *list, User *user) {
    pthread_mutex_lock(&list->mutex);
    list->list = remove_user_from_usr_list(list->list, user);
    pthread_mutex_unlock(&list->mutex);
}
