#include <stdio.h>
#include <unistd.h>
#include <signal.h>

#include "main_server.h"

int server_socket_descriptor;

void close_server()
{
    printf("INFO: \t Closing server\n");
    close(server_socket_descriptor);
    printf("INFO: \t Server closed\n");
    exit(0);
}

int main(int argc, char* argv[])
{
    signal(SIGINT, close_server);
    signal(SIGTERM, close_server);

    printf("INFO: \t Trying to set up server on port %d \n", SERVER_PORT);
    int server_socket_descriptor = SetupServerSocket();
    printf("INFO: \t Server is running on port %d \n", SERVER_PORT);

    UserListHead* list = create_list_head();
    ServerMainLoop(server_socket_descriptor, list);

    printf("INFO: \t Closing server\n");
    close(server_socket_descriptor);
    return(0);
}
