//
// Created by cherit on 12/27/20.
//

#include "list_head.h"

UserListHead *create_list_head() {
    UserListHead* list_head = (UserListHead*)malloc(sizeof(UserListHead));
    list_head->next = NULL;
    list_head->semaphore                 = (pthread_mutex_t)PTHREAD_MUTEX_INITIALIZER;
    return list_head;
}

void add_to_list(UserListHead *list, User *user) {
    pthread_mutex_lock(&list->semaphore);
    list->next = add_to_usr_list(list->next, user);
    pthread_mutex_unlock(&list->semaphore);
}

User *find_on_list(UserListHead *list, char *username) {
    pthread_mutex_lock(&list->semaphore);
    User* user = find_on_usr_list(list->next, username);
    pthread_mutex_unlock(&list->semaphore);
    return user;
}

void remove_user_from_list(UserListHead *list, User *user) {
    pthread_mutex_lock(&list->semaphore);
    list->next = remove_user_from_usr_list(list->next, user);
    pthread_mutex_unlock(&list->semaphore);
}
