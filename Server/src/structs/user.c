//
// Created by cherit on 12/27/20.
//

#include "user.h"

User *create_user() {
    User *user = (User *) malloc(sizeof(User));
    user->username = (char *) malloc(32 * sizeof(char));
    user->connection_descriptor = -1;
    user->calls_to = NULL;
    user->semaphore = (pthread_mutex_t) PTHREAD_MUTEX_INITIALIZER;
    return user;
}


UserList *add_to_usr_list(UserList *list, User *user) {
    if (list == NULL) {
        list = (UserList *) malloc(sizeof(UserList));
        list->user = user;
        list->next = NULL;
    } else if (list->user == NULL)
        list->user = user;
    else
        list->next = add_to_usr_list(list->next, user);
    return list;
}

User *find_on_usr_list(UserList *list, char *username) {
    if (list == NULL || list->user == NULL) return NULL;
    if (strcmp(list->user->username, username) == 0) return list->user;
    return find_on_usr_list(list->next, username);
}

UserList *remove_user_from_usr_list(UserList *list, User *user) {
    if (list == NULL) return NULL;
    if (list->user == user) {
        UserList *newRoot = list->next;
        free(list);
        return newRoot;
    } else {
        list->next = remove_user_from_usr_list(list->next, user);
        return list;
    }
}

void set_username(User *user, char *username) {
    pthread_mutex_lock(&user->semaphore);
    strcpy(user->username, username);
    pthread_mutex_unlock(&user->semaphore);
}
