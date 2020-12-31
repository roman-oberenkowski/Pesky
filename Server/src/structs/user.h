//
// Created by cherit on 12/27/20.
//

#ifndef SERVER_USER_H
#define SERVER_USER_H

#include <sys/ipc.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>

#include <pthread.h>

typedef struct User {
    char *username;
    int connection_descriptor;
    struct User *calls_to;
    pthread_mutex_t semaphore;
} User;

typedef struct UserList {
    User *user;
    struct UserList *next;
} UserList;

User *create_user();

UserList *add_to_usr_list(UserList *list, User *user);

User *find_on_usr_list(UserList *list, char *username);

UserList *remove_user_from_usr_list(UserList *list, User *user);

void set_username(User *user, char *username);

#endif //SERVER_USER_H
