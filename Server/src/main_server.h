//
// Created by cherit on 12/27/20.
//

#ifndef SERVER_MAIN_SERVER_H
#define SERVER_MAIN_SERVER_H

#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <fcntl.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <sys/epoll.h>
#include <stdio.h>
#include <unistd.h>

#include "constants.h"

int SetupServerSocket();

void ServerMainLoop(int server_socket_descriptor, UserListHead* list);

int SetupEpoll ();

void handleConnection(int connection_socket_descriptor, UserListHead* list);

#endif //SERVER_MAIN_SERVER_H
