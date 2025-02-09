#include <errno.h>
#include <pthread.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

void sigintHandler(int sig) {
    printf("Ok!\n");
}

void* threadNoSignal(void* arg) {
    __sigset_t set;
    sigfillset(&set);
    pthread_sigmask(0, &set, NULL);
    sleep(10);

    return NULL;
}

void* threadSIGINT(void* arg) {
    __sigset_t set;
    sigfillset(&set);
    sigdelset(&set, SIGINT);
    pthread_sigmask(0, &set, NULL);
    signal(SIGINT, sigintHandler);
    sleep(10);
    return NULL;
}

void* threadSIGQUIT(void* arg) {
    __sigset_t set;
    int sig;
    sigemptyset(&set);
    sigaddset(&set, SIGQUIT);
    while (1) {
        sigwait(&set, &sig);
        if (sig == SIGQUIT) {
            printf("ne ok :(\n");
        }
    }
    return NULL;
}

int main() {
    printf("%d\n", getpid());
    __sigset_t set;
    sigemptyset(&set);
    sigaddset(&set, SIGQUIT);
    pthread_sigmask(0, &set, NULL);
    pthread_t tid[3];
    pthread_create(&tid[0], NULL, threadNoSignal, NULL);
    pthread_create(&tid[1], NULL, threadSIGINT, NULL);
    pthread_create(&tid[2], NULL, threadSIGQUIT, NULL);
    sleep(5);
    for (size_t i = 0; i < 3; ++i) {
        pthread_join(tid[i], NULL);
    }
    return 0;
}