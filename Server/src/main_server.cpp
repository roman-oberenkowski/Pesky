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
        fprintf(stderr, "Blad przy probie utworzenia gniazda..\n");
        exit(1);
    }
    setsockopt(server_socket_descriptor, SOL_SOCKET, SO_REUSEADDR, (char*)&reuse_addr_val, sizeof(reuse_addr_val));

    bind_result = bind(server_socket_descriptor, (struct sockaddr*)&server_address, sizeof(struct sockaddr));
    if (bind_result < 0)
    {
        fprintf(stderr, "Blad przy probie dowiazania adresu IP i numeru portu do gniazda.\n");
        exit(1);
    }

    listen_sock = listen(server_socket_descriptor, 10);
    if (listen_sock < 0) {
        fprintf(stderr, "Blad przy probie ustawienia wielkosci kolejki.\n");
        exit(1);
    }
    return server_socket_descriptor;
}

void handleConnection(int connection_socket_descriptor, UserListHead* list) {
    int create_result = 0;
    pthread_t thread1;
    printf("New connection");
    struct thread_data_t* t_data = (struct thread_data_t*) malloc(sizeof(struct thread_data_t));
    User* user = create_user();
    user->connection_descriptor  = connection_socket_descriptor;
    add_to_list(list, user);
    t_data->user = user;
    t_data->list = list;
    create_result = pthread_create(&thread1, NULL, ThreadBehavior, (void *)t_data);
    if (create_result){
        printf("Blad przy probie utworzenia watku, kod bledu: %d\n", create_result);
        exit(-1);
    }
}

void ServerMainLoop(int server_socket_descriptor, UserListHead* list) {
    int connection_socket_descriptor;

    while(1)
    {
        connection_socket_descriptor = accept(server_socket_descriptor, NULL, NULL);
        if (connection_socket_descriptor < 0)
        {
            fprintf(stderr, "Blad przy probie utworzenia gniazda dla polaczenia.\n");
            exit(1);
        }
        handleConnection(connection_socket_descriptor, list);
    }
}

int SetupEpoll () {
    int epoll_fd;

    epoll_fd = epoll_create1(0);
    if(epoll_fd == -1) {
        perror("epoll_create error");
        exit(EXIT_FAILURE);
    }

    return epoll_fd;
}