#include <errno.h>
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

struct Moderator {
    int age;
    char* name;
};

void* threadfunc(void* arg) {
    struct Moderator* moder = arg;
    printf("It is my bad moderator: \n");
    printf("His name is %s\n", moder->name);
    printf("His age is %d\n", moder->age);
    return NULL;
}

int main() {
    pthread_t tid;
    struct Moderator Babuboba = {10, "VrodePavlic"};
    // pthread_create(&tid, NULL, threadfunc, &Babuboba);
    // pthread_join(tid, NULL);
    pthread_attr_t attr;
    pthread_attr_init(&attr);
    pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
    pthread_create(&tid, &attr, threadfunc, &Babuboba);
    sleep(5);
    // pthread_join(&tid, NULL);
    return 0;
}
