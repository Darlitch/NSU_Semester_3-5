#include <errno.h>
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

void* threadfunc(void* arg) {
    int* a = malloc(sizeof(int));
    *a = 42;
    return a;
    // char* str = "hello world";
    // return str;
}

int main() {
    pthread_t tid;
    int err = 1;
    int* ret;
    // char* ret;

    err = pthread_create(&tid, NULL, threadfunc, &ret);
    if (err) {
        printf("main: pthread_create() failed: %s\n", strerror(err));
        return -1;
    }
    pthread_join(tid, (void**)&ret);
    printf("%d\n", *ret);
    // printf("%s\n", ret);
    return 0;
}