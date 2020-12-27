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

    printf("Entering thread\n");
    while((size=read(th_data->user->connection_descriptor, incoming_data, 255)) > 0)
    {
        incoming_data[size] = '\0';
        strcat(user_data, incoming_data);

        while (strchr(user_data, MSG_DELIMITER) != NULL)
        {
            message = strtok(user_data, "\n");
            sscanf(message, "type:%[^;];content:%*s;", type);
            sscanf(message, "type:%*[^;];content:%s;", content);
            if(strcmp(type, "close") == 0)
            {
                printf("Closing connection with client\n");
                close((*th_data).user->connection_descriptor);
                break;
            }
            else if(strcmp(type, "set_username") == 0)
            {
                pthread_mutex_lock(&th_data->list->semaphore);
                User* user = find_on_usr_list(th_data->list->next, content);
                if (user == NULL)
                    set_username(th_data->user, content);
                pthread_mutex_unlock(&th_data->list->semaphore);
                printf("Changed username to: %s\n", content);
            }
            else
            {
                //TODO PROCESS MESSAGE
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
            memset(type,0,sizeof(type));
            memset(content,0,sizeof(content));
        }
    }
    printf("Exiting thread");
    pthread_detach(pthread_self());
    remove_user_from_list(th_data->list, th_data->user);
    free(th_data->user);
    free(t_data);
    pthread_exit(NULL);
}

