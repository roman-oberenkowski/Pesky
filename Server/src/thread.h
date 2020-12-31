//
// Created by cherit on 12/27/20.
//

#ifndef SERVER_THREAD_H
#define SERVER_THREAD_H

#include <pthread.h>
#include <sys/epoll.h>
#include <stdio.h>
#include <fcntl.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>
#include <regex.h>
#include "constants.h"
#include "structs/list_head.h"

struct thread_data_t {
    User *user;
    UserListHead *list;
};

void exitThread(struct thread_data_t *thread_data);

void *ThreadBehavior(void *t_data);


#endif //SERVER_THREAD_H
