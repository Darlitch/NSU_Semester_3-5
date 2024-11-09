#include <errno.h>
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

void* threadfunc(void* arg) {
    printf("%ld\n", pthread_self());
    pthread_detach(pthread_self());
    return NULL;
}

int main() {
    pthread_t tid;
    // pthread_attr_t attr;
    // pthread_attr_init(&attr);
    // pthread_attr_setdetachstate(&attr,PTHREAD_CREATE_DETACHED);
    while(1) {
        pthread_create(&tid, NULL, threadfunc, NULL);
        // pthread_create(&tid, &attr, threadfunc, NULL);
        pthread_join(tid, NULL);
        sleep(5);
    }
    return 0;
}