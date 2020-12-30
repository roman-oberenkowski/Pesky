//
// Created by cherit on 12/27/20.
//
#include "thread.h"
#include "messaging.h"

void exitThread(struct thread_data_t *thread_data)
{
    int descriptor;
    pthread_mutex_t semaphore;
    pthread_mutex_lock(&thread_data->user->semaphore);
    semaphore = thread_data->user->semaphore;
    descriptor = thread_data->user->connection_descriptor;
    remove_user_from_list(thread_data->list, thread_data->user);
    if (thread_data->user->calls_to != NULL)
    {
        pthread_mutex_lock(&thread_data->user->calls_to->semaphore);
        sendDisconnectMessage(thread_data->user->calls_to, thread_data->user, 0);
        thread_data->user->calls_to->calls_to = NULL;
        pthread_mutex_unlock(&thread_data->user->calls_to->semaphore);
    }
    free(thread_data->user->username);
    free(thread_data->user);
    pthread_mutex_unlock(&semaphore);
    pthread_attr_destroy((pthread_attr_t *) &semaphore);
    close(descriptor);
    free(thread_data);
    pthread_exit(NULL);
}

void *ThreadBehavior(void *t_data)
{
    char user_data[MAX_INCOMING_SIZE*3] = "";
    char incoming_data[MAX_INCOMING_SIZE];
    char *message;

    int size;
    int connection_status = 1;

    struct thread_data_t *th_data = (struct thread_data_t*)t_data;

    while((size=read(th_data->user->connection_descriptor, incoming_data, MAX_INCOMING_SIZE)) > 0)
    {
        incoming_data[size] = '\0';
        strcat(user_data, incoming_data);

        while (strchr(user_data, MSG_DELIMITER_CHR) != NULL)
        {
            message = strtok(user_data, MSG_DELIMITER_STR);
            connection_status = processMessage(th_data, message);
            if (connection_status == 0)
                break;
            message = strtok(NULL, MSG_DELIMITER_STR);
            if (message == NULL)
            {
                memset(user_data,0,strlen(user_data));
            }
            else
            {
                strcpy(user_data, message);
            }
        }
        if (connection_status == 0)
            break;
    }
    pthread_detach(pthread_self());
    exitThread(th_data);
}

