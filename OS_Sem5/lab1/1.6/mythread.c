#define _GNU_SOURCE
#include "mythread.h"

#include <fcntl.h>
#include <sched.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>
#include <sys/types.h>
#include <unistd.h>

#define PAGE 4096
#define STACK_SIZE 3 * PAGE

void *create_stack(off_t size, int mytid, mythread_t* thread) {
    // char stack_file[128];
    // int fd;
    void *stack;

    // fd = open(stack_file, O_RDWR | O_CREAT, 0660);
    // ftruncate(fd, 0);
    // ftruncate(fd, size);
    stack = mmap(NULL, size, PROT_NONE, MAP_PRIVATE | MAP_ANONYMOUS | MAP_STACK, -1, 0);
    mprotect(stack + PAGE, STACK_SIZE - PAGE, PROT_READ | PROT_WRITE);
    *thread = (mythread_t)(stack + STACK_SIZE - sizeof(mythread_struct_t));
    stack = (void *)*thread;
    // close(fd);
    return stack;
}

int thread_startup(void *arg) {
    mythread_t thread = (mythread_t)arg;
    thread->retval = thread->start_routine(thread->arg);
    // printf("aaaaa");
    thread->finished = 1;
    while (!thread->joined) {
        sleep(1);
    }
    return 0;
}

int mypthread_create(mythread_t *thread, void *(*start_routine)(void *), void *arg) {
    static int mytid = 0;
    mythread_t thr;
    void *child_stack;
    int child_pid;

    mytid++;

    printf("mythread_create: Creating pthread %d\n", mytid);
    child_stack = create_stack(STACK_SIZE, mytid, &thr);
    // mprotect(child_stack + PAGE, STACK_SIZE - PAGE, PROT_READ | PROT_WRITE);

    // memset(child_stack + PAGE, 0x7f, STACK_SIZE - PAGE);

    // thr = (mythread_t)(child_stack + STACK_SIZE - sizeof(mythread_struct_t));
    thr->mytid = mytid;
    thr->arg = arg;
    thr->start_routine = start_routine;
    thr->retval = NULL;
    thr->finished = 0;
    thr->joined = 0;

    // child_stack = (void *)thr;

    printf("child stack: %p;  mythread: %p\n", child_stack, thr);
    child_pid = clone(thread_startup, child_stack, CLONE_VM | CLONE_FILES | CLONE_THREAD | CLONE_FS | CLONE_SIGHAND, thr);
    if (child_pid == -1) {
        printf("Clone failed\n");
        exit(-1);
    }
    *thread = thr;

    // create_stack
    // thread_startup
}

void mypthread_join(mythread_t mytid, void **retval) {
    mythread_struct_t *thread = mytid;

    printf("Thread_join: waiting for the thread %d to finish\n", thread->mytid);

    while (!thread->finished) {
        usleep(1);
    }

    printf("thread_join: the thread %d finished\n", thread->mytid);
    if (retval != NULL) {
        // *retval = thread->retval;
        printf("111111111111111111111");
    }
    thread->joined = 1;
}