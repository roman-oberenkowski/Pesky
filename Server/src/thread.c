//
// Created by cherit on 12/27/20.
//
#include "thread.h"

void processMessage(char* message);

void *ThreadBehavior(void *t_data)
{
    char user_data[512];
    char incoming_data[255];
    int size;
    struct thread_data_t *th_data = (struct thread_data_t*)t_data;

    printf("Entering thread");
    while((size=read(th_data->connection_socket_descriptor, incoming_data, 255)) > 0)
    {
        incoming_data[size] = '\0';
        strcat(user_data, incoming_data);
        if(strcmp(incoming_data, "close\n") == 0)
        {
            printf("Closing connection with client\n");
            close((*th_data).connection_socket_descriptor);
            break;
        }
        else
        {
            printf("> %s\n", incoming_data);
        }
    }
    printf("Exiting thread");
    free(t_data);
    pthread_detach(pthread_self());
    pthread_exit(NULL);
}

