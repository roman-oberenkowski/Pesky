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

void getContent(char message[], char* content);

void getType(char message[], char* type);

void sendMessage(User* user, char message[], int size);

void sendConfirmMessage(User* user, char content[]);

void sendErrorMessage(User* user, char content[]);

void forwardMessage(User* user, char message[]);

int processMessage(struct thread_data_t *thread_data, char message[]);

#endif //SERVER_MESSAGING_H
