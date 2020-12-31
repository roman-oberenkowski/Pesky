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

/**
 * User structure
 */
typedef struct User {
    char *username;
    int connection_descriptor;
    struct User *calls_to;
    pthread_mutex_t semaphore;
} User;

/**
 * Linked list of users
 */
typedef struct UserList {
    User *user;
    struct UserList *next;
} UserList;

/**
 * Create user empty fields
 * @return user
 */
User *create_user();

/**
 * Adds given user to list
 * @param list of users
 * @param user to add
 * @return Starts of user list
 */
UserList *add_to_usr_list(UserList *list, User *user);

/**
 * Finds user with a given username on a list
 * @param list of users
 * @param username of user
 * @return user
 */
User *find_on_usr_list(UserList *list, char *username);

/**
 * Removes given user from user list
 * @param list of users
 * @param user to remove
 * @return beginning of user list
 */
UserList *remove_user_from_usr_list(UserList *list, User *user);

/**
 * Set username of a given user
 * @param user
 * @param username
 */
void set_username(User *user, char *username);

#endif //SERVER_USER_H
