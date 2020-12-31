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

/**
 * Head of users list
 */
typedef struct UserListHead {
    pthread_mutex_t semaphore;
    struct UserList *next;
} UserListHead;

/**
 * Creates user list header
 * @return Pointer to user list header
 */
UserListHead *create_list_head();

/**
 * Adds user to list
 * @param list of users
 * @param user
 */
void add_to_list(UserListHead *list, User *user);

/**
 * Finds user on list by username
 * @param list of users
 * @param username of a looking
 * @return user with a given username
 */
User *find_on_list(UserListHead *list, char *username);

/**
 * Removes user from lisg
 * @param list of users
 * @param user to remove
 */
void remove_user_from_list(UserListHead *list, User *user);

#endif //SERVER_LIST_HEAD_H
