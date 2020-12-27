#include <stdio.h>
#include <unistd.h>
#include <signal.h>

#include "thread.h"
#include "main_server.cpp"

int server_socket_descriptor;
void close_server()
{
    close(server_socket_descriptor);
    exit(0);
}

int main(int argc, char* argv[])
{
    signal(SIGINT, close_server);
    signal(SIGTERM, close_server);

    int server_socket_descriptor = SetupServerSocket();
    UserListHead* list = create_list_head();
    ServerMainLoop(server_socket_descriptor, list);

    close(server_socket_descriptor);
    return(0);
}
