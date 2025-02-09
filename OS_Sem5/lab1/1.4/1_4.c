#include <errno.h>
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

void cleanupFunc(void* arg) {
    free(arg);
    printf("12345");
}

void* threadfunc(void* arg) {
    // int temp = 0;
    
    pthread_setcancelstate(PTHREAD_CANCEL_DISABLE, NULL);
    char* str = (char*) malloc(sizeof(char) * 12);
    strcpy(str, "hello world");
    pthread_cleanup_push(cleanupFunc, str);
    pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, NULL);
    
    while(1) {
        // printf("This is some text\n");
        // sleep(1);

        // temp++;
        // sleep(1);
        // printf("%d\n", temp);

        printf("%s\n", str);
        sleep(1);
    }
    pthread_cleanup_pop(1);
    return NULL;
}

int main() {
    pthread_t tid;
    pthread_create(&tid, NULL, threadfunc, NULL);
    sleep(5);
    pthread_cancel(tid);
    pthread_join(tid, NULL);
    return 0;
}