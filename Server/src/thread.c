//
// Created by cherit on 12/27/20.
//
#include "thread.h"

void processMessage(char* message);


void *ThreadBehavior(void *t_data)
{
    char user_data[512] = "";
    char incoming_data[255];
    int size;
    char type[16];
    char content[512];
    char *message;
    struct thread_data_t *th_data = (struct thread_data_t*)t_data;

    printf("Entering thread");
    while((size=read(th_data->connection_socket_descriptor, incoming_data, 255)) > 0)
    {
        incoming_data[size] = '\0';
        strcat(user_data, incoming_data);

        while (strchr(user_data, MSG_DELIMITER) != NULL)
        {
            message = strtok(user_data, "\n");
            sscanf(message, "type:%[^;];content:%*s;\n", type);
            sscanf(message, "type:%*[^;];content:%s;\n", content);

            if(strcmp(message, "close") == 0)
            {
                printf("Closing connection with client\n");
                close((*th_data).connection_socket_descriptor);
                break;
            }
            else
            {
                printf("> %s %s\n", type, content);
            }
            message = strtok(NULL, "\n");
            if (message == NULL)
            {
                memset(user_data,0,strlen(user_data));
            }
            else
            {
                strcpy(user_data, message);
            }
        }
    }
    printf("Exiting thread");
    free(t_data);
    pthread_detach(pthread_self());
    pthread_exit(NULL);
}

