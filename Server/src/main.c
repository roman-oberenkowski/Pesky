#include <stdio.h>
#include <unistd.h>
#include <signal.h>

#include "main_server.h"

int server_socket_descriptor;
UserListHead *list = NULL;
void close_server() {
    printf("INFO: \t Closing server\n");
    User* user;
    if (list != NULL)
    {
        pthread_mutex_lock(&list->semaphore);
        while (list->next != NULL)
        {
            pthread_mutex_lock(&list->next->user->semaphore);
            user = list->next->user;
            shutdown(user->connection_descriptor, SHUT_RDWR);
            read(user->connection_descriptor, NULL, NULL);
            close(user->connection_descriptor);
            list->next = remove_user_from_usr_list(list->next, user);
            pthread_mutex_unlock(&user->semaphore);
            free(user);
        }
        pthread_mutex_unlock(&list->semaphore);
    }
    free(list);
    close(server_socket_descriptor);
    printf("INFO: \t Server closed\n");
    exit(0);
}

int main(int argc, char *argv[]) {
    signal(SIGINT, close_server);
    signal(SIGTERM, close_server);
    // Preventing signals
    signal(SIGCHLD, NULL);

    printf("INFO: \t Trying to set up server on port %d \n", SERVER_PORT);
    int server_socket_descriptor = SetupServerSocket();
    printf("INFO: \t Server is running on port %d \n", SERVER_PORT);

    list = create_list_head();
    ServerMainLoop(server_socket_descriptor, list);

    printf("INFO: \t Closing server\n");
    close(server_socket_descriptor);
    return (0);
}
