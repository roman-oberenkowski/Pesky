//
// Created by cherit on 12/30/20.
//

#include "messaging.h"

void getContent(char *message, char *content) {
    if (message != NULL && strlen(message) > 7)
        sscanf(message, "type:%*[^;];content:%s", content);
    else
        strcpy(content, "");
}

void getType(char *message, char *type) {
    if (message != NULL && strlen(message) > 7)
        sscanf(message, "type:%[^;];content:%*s", type);
    else
        strcpy(type, "undefined");
}

void sendMessage_lock(User *user, char message[], int size) {
    if (user != NULL) {
        pthread_mutex_lock(&user->mutex);
        write(user->connection_descriptor, message, size);
        if (DEBUG == 1)
            printf("INFO: \t Sent message with locking\n");
        pthread_mutex_unlock(&user->mutex);
    }
}

void sendMessage(User *user, char message[], int size) {
    if (user != NULL) {
        write(user->connection_descriptor, message, size);
        if (DEBUG == 1)
            printf("INFO: \t Sent message without locking\n");
    }
}

void sendConfirmMessage(User *user, char content[], int lock) {
    char message[MAX_CONFIRM_SIZE] = "type:confirm;content:";
    strcat(message, content);
    strcat(message, MSG_DELIMITER_STR);
    if (lock == 1)
        sendMessage_lock(user, message, MAX_CONFIRM_SIZE);
    else
        sendMessage(user, message, MAX_CONFIRM_SIZE);
}

void sendErrorMessage(User *user, char content[], int lock) {
    char message[MAX_ERROR_SIZE] = "type:error;content:";
    strcat(message, content);
    strcat(message, MSG_DELIMITER_STR);
    if (lock == 1)
        sendMessage_lock(user, message, MAX_ERROR_SIZE);
    else
        sendMessage(user, message, MAX_ERROR_SIZE);
}

void sendJoinedMessage(User *receiver_user, User *caller_user, int lock) {
    char message[MAX_CONFIRM_SIZE] = "type:joined;content:";
    strcat(message, caller_user->username);
    strcat(message, MSG_DELIMITER_STR);
    if (lock == 1)
        sendMessage_lock(receiver_user, message, MAX_CONFIRM_SIZE);
    else
        sendMessage(receiver_user, message, MAX_CONFIRM_SIZE);
}

void sendDisconnectMessage(User *receiver_user, User *caller_user, int lock) {
    char message[MAX_CONFIRM_SIZE] = "type:disconnect;content:";
    strcat(message, caller_user->username);
    strcat(message, MSG_DELIMITER_STR);
    if (lock == 1)
        sendMessage_lock(receiver_user, message, MAX_CONFIRM_SIZE);
    else
        sendMessage(receiver_user, message, MAX_CONFIRM_SIZE);
}

int processSetUsernameMessage(struct thread_data_t *thread_data, char *message, char *content) {
    getContent(message, content);
    if (strcmp(content, "") == 0) {
        sendErrorMessage(thread_data->user, "Given incorrect content data", 1);
        if (DEBUG == 1)
            printf("ERROR: \t Given incorrect content data\n");
    } else {
        pthread_mutex_lock(&thread_data->list->mutex);
        User *user = find_on_usr_list(thread_data->list->list, content);
        if (user == NULL) {
            set_username(thread_data->user, content);
            sendConfirmMessage(thread_data->user, "Successfully changed username", 1);
            if (DEBUG == 1)
                printf("INFO: \t Changed username to: \"%s\"\n", content);
        } else {
            sendErrorMessage(thread_data->user, "User with this username already exists", 1);
            if (DEBUG == 1)
                printf("ERROR: \t User with \"%s\" username already exists\n", content);
        }
        pthread_mutex_unlock(&thread_data->list->mutex);
    }
    return 1;
}

int processCallToMessage(struct thread_data_t *thread_data, char *message, char *content) {
    pthread_mutex_lock(&thread_data->user->mutex);
    if (thread_data->user->connected_with != NULL) {
        sendErrorMessage(thread_data->user, "Still connected to another user.", 0);
        if (DEBUG == 1)
            printf("ERROR: \t User \"%s\" is still connected with someone else\n", thread_data->user->username);
        pthread_mutex_unlock(&thread_data->user->mutex);
        return 1;
    }
    if (strcmp(thread_data->user->username, "") == 0) {
        sendErrorMessage(thread_data->user, "User has no username.", 0);
        if (DEBUG == 1)
            printf("ERROR: \t User has no username\n");
        pthread_mutex_unlock(&thread_data->user->mutex);
        return 1;
    }
    pthread_mutex_unlock(&thread_data->user->mutex);

    getContent(message, content);
    if (strcmp(content, "") != 0) {
        if (strcmp(thread_data->user->username, content) == 0) {
            sendErrorMessage(thread_data->user, "User cannot call himself.", 0);
            if (DEBUG == 1) {
                printf("ERROR: \t User \"%s\" tried to called himself\n", thread_data->user->username);
                return 1;
            }
        }
        pthread_mutex_lock(&thread_data->list->mutex);
        User *user = find_on_usr_list(thread_data->list->list, content);
        pthread_mutex_lock(&thread_data->user->mutex);
        if (user != NULL) {
            pthread_mutex_lock(&user->mutex);
            if (user->connected_with == NULL) {
                user->connected_with = thread_data->user;
                thread_data->user->connected_with = user;
                sendJoinedMessage(user, thread_data->user, 0);
                sendConfirmMessage(thread_data->user, "Successfully called user", 0);
                if (DEBUG == 1)
                    printf("INFO: \t User \"%s\" called to \"%s\"\n", thread_data->user->username, user->username);
            } else {
                sendErrorMessage(thread_data->user, "User is in another conversation", 0);
                if (DEBUG == 1)
                    printf("ERROR: \t User \"%s\" is in another conversation\n", user->username);
            }
            pthread_mutex_unlock(&user->mutex);
        } else {
            sendErrorMessage(thread_data->user, "User with this username does not exists", 0);
            if (DEBUG == 1)
                printf("ERROR: \t User \"%s\" does not exists\n", content);
        }
        pthread_mutex_unlock(&thread_data->user->mutex);
        pthread_mutex_unlock(&thread_data->list->mutex);
    } else {
        sendErrorMessage(thread_data->user, "Given incorrect content data", 1);
        if (DEBUG == 1)
            printf("ERROR: \t Given incorrect content data\n");
    }
    return 1;
}

int processForwardMessage(User *user, char *message) {
    if (user != NULL) {
        pthread_mutex_lock(&user->mutex);
        if (user->connected_with != NULL) {
            pthread_mutex_lock(&user->connected_with->mutex);
            write(user->connected_with->connection_descriptor, message, strlen(message) * sizeof(char));
            if (DEBUG == 1)
                printf("INFO: \t Forwarded message \"%s\" from \"%s\" to \"%s\"\n", message, user->username,
                       user->connected_with->username);
            pthread_mutex_unlock(&user->connected_with->mutex);
        }
        pthread_mutex_unlock(&user->mutex);
    }
    return 1;
}

int processIncorrectMessage(struct thread_data_t *thread_data, char *message, const char *type, char *content) {
    sendErrorMessage(thread_data->user, message, 1);
    if (DEBUG == 1) {
        getContent(message, content);
        printf("INFO: \t Recieved incorrect message type> \"%s\" content> \"%s\"\n", type, content);
    }
    return 1;
}

int processMessage(struct thread_data_t *thread_data, char message[]) {
    char type[MAX_TYPE_SIZE] = "";
    char content[MAX_CONTENT_SIZE] = "";

    if (DEBUG == 1)
        printf("INFO: \t Processing message: \"%s\"\n", message);

    getType(message, type);
    if (strcmp(type, "call_to") == 0)
        return processCallToMessage(thread_data, message, content);
    else if (strcmp(type, "set_username") == 0)
        return processSetUsernameMessage(thread_data, message, content);
    else if (strcmp(type, "audio") == 0 || strcmp(type, "video") == 0)
        return processForwardMessage(thread_data->user, message);
    else if (strcmp(type, "disconnect") == 0)
        return 0;
    else
        return processIncorrectMessage(thread_data, message, type, content);
}
