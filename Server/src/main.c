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
    struct thread_data_t t_data;
    signal(SIGINT, close_server);
    signal(SIGTERM, close_server);

    int server_socket_descriptor = SetupServerSocket();
    ServerMainLoop(server_socket_descriptor);

    close(server_socket_descriptor);
    return(0);
}
