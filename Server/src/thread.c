//
// Created by cherit on 12/27/20.
//
#include "thread.h"
#include "messaging.h"

void exitThread(struct thread_data_t *thread_data) {
    int descriptor;
    pthread_mutex_t semaphore;
    pthread_mutex_lock(&thread_data->user->mutex);
    if (DEBUG == 1)
        printf("INFO: \t Disconnecting user %s\n", thread_data->user->username);

    semaphore = thread_data->user->mutex;
    descriptor = thread_data->user->connection_descriptor;

    remove_user_from_list(thread_data->list, thread_data->user);
    if (thread_data->user->connected_with != NULL) {
        pthread_mutex_lock(&thread_data->user->connected_with->mutex);

        if (DEBUG == 1)
            printf("INFO: \t Closing chat between %s and %s\n", thread_data->user->username,
                   thread_data->user->connected_with->username);
        sendDisconnectMessage(thread_data->user->connected_with, thread_data->user, 0);
        thread_data->user->connected_with->connected_with = NULL;

        pthread_mutex_unlock(&thread_data->user->connected_with->mutex);
    }

    free(thread_data->user->username);
    free(thread_data->user);
    pthread_mutex_unlock(&semaphore);
    shutdown(descriptor, SHUT_RDWR);
    close(descriptor);
    free(thread_data);
    if (DEBUG == 1)
        printf("INFO: \t Closed thread successfully\n");
    pthread_exit(NULL);
}

void *ThreadBehavior(void *t_data) {
    char user_data[MAX_INCOMING_SIZE * 3] = "";
    char incoming_data[MAX_INCOMING_SIZE];
    char *message_rests;
    char *message;

    int size;
    int connection_status = 1;

    struct thread_data_t *th_data = (struct thread_data_t *) t_data;

    while ((size = read(th_data->user->connection_descriptor, incoming_data, MAX_INCOMING_SIZE)) > 0) {
        if (DEBUG == 1)
            printf("INFO: \t Recieved message: %s\n", incoming_data);
        if (size != MAX_INCOMING_SIZE)
            incoming_data[size] = '\0';
        strcat(user_data, incoming_data);

        if (strchr(user_data, MSG_DELIMITER_CHR) != NULL)
        {
            message_rests = user_data;
            do {
                message = strtok_r(message_rests, MSG_DELIMITER_STR, &message_rests);
                connection_status = processMessage(th_data, message);
                if (connection_status == 0)
                    break;
            } while (strlen(message_rests) > 0 && strchr(message_rests, MSG_DELIMITER_CHR) != NULL);
            if (connection_status == 0)
                break;
            if (strlen(message_rests) > 0)
                strcpy(user_data, message_rests);
            else
                memset(user_data, 0, strlen(user_data));
        }
    }
    exitThread(th_data);
    pthread_detach(pthread_self());
    return 0;
}
