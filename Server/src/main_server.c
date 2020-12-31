//
// Created by cherit on 12/27/20.
//

#include "main_server.h"

int SetupServerSocket() {

    struct sockaddr_in server_address;
    int bind_result, listen_sock;
    char reuse_addr_val = 1;
    int server_socket_descriptor;

    memset(&server_address, 0, sizeof(struct sockaddr));
    server_address.sin_family = AF_INET;
    server_address.sin_addr.s_addr = htonl(INADDR_ANY);
    server_address.sin_port = htons(SERVER_PORT);

    server_socket_descriptor = socket(AF_INET, SOCK_STREAM, 0);
    if (server_socket_descriptor < 0)
    {
        fprintf(stderr, "ERROR: \t Unable to create socket. Error code: %d\n", server_socket_descriptor);
        exit(EXIT_FAILURE);
    }
    setsockopt(server_socket_descriptor, SOL_SOCKET, SO_REUSEADDR, (char*)&reuse_addr_val, sizeof(reuse_addr_val));

    bind_result = bind(server_socket_descriptor, (struct sockaddr*)&server_address, sizeof(struct sockaddr));
    if (bind_result < 0)
    {
        fprintf(stderr, "ERROR: \t Unable to join IP address and port number. Error code: %d\n", bind_result);
        exit(EXIT_FAILURE);
    }

    listen_sock = listen(server_socket_descriptor, 10);
    if (listen_sock < 0) {
        fprintf(stderr, "ERROR: \t Unable to set queue size. Error code: %d\n", listen_sock);
        exit(EXIT_FAILURE);
    }
    printf("INFO: \t Server is ready to listening at: %d\n", server_socket_descriptor);
    return server_socket_descriptor;
}

void handleConnection(int connection_socket_descriptor, UserListHead* list) {
    int create_result = 0;
    pthread_t thread1;
    printf("INFO: \t Server recieved a new connection\n");
    struct thread_data_t* t_data = (struct thread_data_t*) malloc(sizeof(struct thread_data_t));
    User* user = create_user();
    user->connection_descriptor  = connection_socket_descriptor;
    add_to_list(list, user);
    t_data->user = user;
    t_data->list = list;
    create_result = pthread_create(&thread1, NULL, ThreadBehavior, (void *)t_data);
    if (create_result)
    {
        fprintf(stderr, "ERROR: \t Unable to create thread. Error code: %d\n", create_result);
        exit(EXIT_FAILURE);
    }
    else
    {
        printf("INFO: \t Thread successfully created \n");
    }
}

void ServerMainLoop(int server_socket_descriptor, UserListHead* list) {
    int connection_socket_descriptor;

    while(1)
    {
        connection_socket_descriptor = accept(server_socket_descriptor, NULL, NULL);
        if (connection_socket_descriptor < 0)
        {
            fprintf(stderr, "ERROR: \t Unable to create socket for connection\n");
            exit(EXIT_FAILURE);
        }
        handleConnection(connection_socket_descriptor, list);
    }
}

int SetupEpoll () {
    int epoll_fd;

    epoll_fd = epoll_create1(0);
    if(epoll_fd == -1) {
        fprintf(stderr, "ERROR: \t Unable to create epoll. Error code: %d\n", epoll_fd);
        exit(EXIT_FAILURE);
    }
    return epoll_fd;
}