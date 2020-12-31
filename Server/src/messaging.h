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

/**
 * Gets message content
 * @param message to process
 * @param content found
 */
void getContent(char message[], char *content);

/**
 * Gets message type
 * @param message to process
 * @param type found
 */
void getType(char message[], char *type);

/**
 * Send message to user without locking
 * @param user message receiver
 * @param message to send
 * @param size of a message
 */
void sendMessage(User *user, char message[], int size);

/**
 * Send message to user with locking
 * @param user message receiver
 * @param message to send
 * @param size of a message
 */
void sendMessage_lock(User *user, char message[], int size);

/**
 * Send confirmation message to user with a given content and lock
 * @param user message receiver
 * @param content of a message
 * @param lock if user should be locked or not
 */
void sendConfirmMessage(User *user, char content[], int lock);

/**
 * Send error message to user with a given content and lock
 * @param user message receiver
 * @param content of a message
 * @param lock if user should be locked or not
 */
void sendErrorMessage(User *user, char content[], int lock);

/**
 * Send join confirmation message to user with a given content and lock
 * @param user message receiver
 * @param content of a message
 * @param lock if user should be locked or not
 */
void sendJoinedMessage(User *receiver_user, User *caller_user, int lock);

/**
 * Send disconnect message to user and lock
 * @param receiver_user message receiver
 * @param caller_user message sender
 * @param lock if user should be locked or not
 */
void sendDisconnectMessage(User *receiver_user, User *caller_user, int lock);

/**
 * Process message with setting up username
 * @param thread_data thread's data
 * @param message from user
 * @param content from user
 * @return if user should be still connected
 */
int processSetUsernameMessage(struct thread_data_t *thread_data, char *message, char *content);

/**
 * Process message with calling to another user
 * @param thread_data thread's data
 * @param message from user
 * @param content from user
 * @return if user should be still connected
 */
int processCallToMessage(struct thread_data_t *thread_data, char *message, char *content);

/**
 * Processing message forwarding to another user
 * @param user sender user
 * @param message message from user
 * @return if user should be still connected
 */
int processForwardMessage(User *user, char *message);

/**
 * Process incorrect message
 * @param thread_data thread's data
 * @param message from user
 * @param type of message
 * @param content of message
 * @return if user should be still connected
 */
int processIncorrectMessage(struct thread_data_t *thread_data, char message[], const char *type, char *content);

/**
 * Process message from user
 * @param thread_data thread's data
 * @param message message
 * @return if user should be still connected
 */
int processMessage(struct thread_data_t *thread_data, char message[]);

#endif //SERVER_MESSAGING_H
