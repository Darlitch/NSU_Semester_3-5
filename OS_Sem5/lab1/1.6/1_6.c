#include "mythread.h"

#include <unistd.h>
// #include <pthread.h>
#include <stdio.h>

void *mythread(void *arg) {
    printf("mythread [%d %d]: Hello from mythread!\n", getpid(), getppid());
    // int localus = 1;
    return NULL;
}

int main() {
    mythread_t tid;
    printf("mythread [%d %d]: Hello from main!\n", getpid(), getppid());
    mypthread_create(&tid, mythread, NULL);
    sleep(10);
    mypthread_join(tid, NULL);
}