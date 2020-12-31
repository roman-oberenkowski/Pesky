//
// Created by cherit on 12/30/20.
//

#ifndef SERVER_MESSAGING_H
#define SERVER_MESSAGING_H

#include <pthread.h>
#include <stdio.h>
#include <fcntl.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>
#include <regex.h>
#include "constants.h"
#include "structs/list_head.h"
#include "thread.h"

void getContent(char message[], char *content);

void getType(char message[], char *type);

void sendMessage(User *user, char message[], int size);

void sendMessage_lock(User *user, char message[], int size);

void sendConfirmMessage(User *user, char content[], int lock);

void sendErrorMessage(User *user, char content[], int lock);

void sendJoinedMessage(User *receiver_user, User *caller_user, int lock);

void sendDisconnectMessage(User *receiver_user, User *caller_user, int lock);

int processSetUsernameMessage(struct thread_data_t *thread_data, char *message, char *content);

int processCallToMessage(struct thread_data_t *thread_data, char *message, char *content);

int processForwardMessage(User *user, char *message);

int processIncorrectMessage(struct thread_data_t *thread_data, char message[], const char *type, char *content);

int processMessage(struct thread_data_t *thread_data, char message[]);

#endif //SERVER_MESSAGING_H
