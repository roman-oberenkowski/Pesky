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
#include "thread.h"

/**
 * Set up server socket
 * @return server's socket description
 */
int SetupServerSocket();

/**
 * Main server operation loop
 * @param server_socket_descriptor server description
 * @param list head of users list
 */
void ServerMainLoop(int server_socket_descriptor, UserListHead *list);

/**
 * Set up server's epoll
 * @return server's epoll descriptor
 */
int SetupEpoll();

/**
 * Handles connection with a user, creating separate thread for them
 * @param connection_socket_descriptor descriptor of a given collection
 * @param list head of users list
 */
void handleConnection(int connection_socket_descriptor, UserListHead *list);

#endif //SERVER_MAIN_SERVER_H
