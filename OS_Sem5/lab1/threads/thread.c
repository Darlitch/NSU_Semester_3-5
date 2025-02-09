#define _GNU_SOURCE
#include <errno.h>
#include <pthread.h>
#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <unistd.h>

int GLOBUS = 1;

void *mythread(void *arg) {
    printf("mythread [%d %d %d]: Hello from mythread!\n", getpid(), getppid(), gettid());
    // int localus = 1;
    return NULL;
}

void *myfunc(void *arg) {
    printf("Hello from MyFunc [%d %d %d]\n", getpid(), getppid(), gettid());
    printf("-Thread ID[] = %ld\n", pthread_self());
    int localus = 1;
    int static statLocal = -2;
    int const constLocalus = -1;
    localus++;
    GLOBUS++;
    printf("localus: %d\n", localus);
    printf("GLOBUS: %d\n", GLOBUS);
    sleep(20);
    printf("%p %p %p %p\n", &localus, &statLocal, &constLocalus, &GLOBUS);
    // printf("123\n");
    sleep(200);
    return NULL;
}

int main() {
    pthread_t tid[6];
    int err[] = {1, 1, 1, 1, 1, 1};
    // int localus = 1;

    printf("main [%d %d %d]: Hello from main!\n", getpid(), getppid(), gettid());
    printf("lalala\n");
    err[0] = pthread_create(&tid[0], NULL, mythread, NULL);
    if (err[0]) {
        printf("main: pthread_create() failed: %s\n", strerror(err[0]));
        return -1;
    }
    for (size_t i = 1; i < 6; ++i) {
        err[i] = pthread_create(&tid[i], NULL, myfunc, NULL);
        if (err[i]) {
            printf("main: pthread_create(%lu) failed: %s\n", i, strerror(err[i]));
            return -1;
        }
    }
    // int localus = 1;
    // err=1;
    for (size_t i = 0; i < 6; ++i) {
        printf("Thread ID[%lu] in main = %ld\n", i, tid[i]);
        pthread_join(tid[i], NULL);
    }
    printf("lalalaend");
    // sleep(200);
    return 0;
}
