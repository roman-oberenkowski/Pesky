//
// Created by cherit on 12/27/20.
//

#ifndef SERVER_LIST_HEAD_H
#define SERVER_LIST_HEAD_H

#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include "user.h"

typedef struct UserListHead {
    pthread_mutex_t semaphore;
    struct UserList *next;
} UserListHead;

UserListHead *create_list_head();

void add_to_list(UserListHead *list, User *user);

User *find_on_list(UserListHead *list, char *username);

void remove_user_from_list(UserListHead *list, User *user);

#endif //SERVER_LIST_HEAD_H
