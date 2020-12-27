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

struct thread_data_t
{
    int connection_socket_descriptor;
};

void *ThreadBehavior(void *t_data);



#endif //SERVER_THREAD_H
