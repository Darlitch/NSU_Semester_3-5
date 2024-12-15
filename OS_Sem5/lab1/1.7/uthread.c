#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>
#include <sys/types.h>
#include <unistd.h>

#include "uthread_t.h"

#define MAX_THREADS 4
#define PAGE 4096
#define STACK_SIZE PAGE * 2

uthread_t *uthreads[MAX_THREADS];
// static ucontext_t mainContext;
size_t uthread_count = 0;
size_t uthread_cur = 0;

void *create_stack(off_t size, uthread_t** thread) {
    // char stack_file[128];
    // int fd;
    void *stack;

    // fd = open(stack_file, O_RDWR | O_CREAT, 0660);
    // ftruncate(fd, 0);
    // ftruncate(fd, size);
    stack = mmap(NULL, size, PROT_READ|PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS | MAP_STACK, -1, 0);
    // mprotect(stack + PAGE, STACK_SIZE - PAGE, PROT_READ | PROT_WRITE);
    *thread = (uthread_t*)(stack + STACK_SIZE - sizeof(uthread_t));
    stack = (void *)*thread;
    // close(fd);
    return stack;
}

void schedule(void) {
    ucontext_t* cur_ctx;
    ucontext_t* next_ctx;
    cur_ctx = &(uthreads[uthread_cur]->uctx);
    uthread_cur = (uthread_cur + 1) % uthread_count;
    next_ctx = &(uthreads[uthread_cur]->uctx);
    swapcontext(cur_ctx, next_ctx);
}

void thread_startup(void) {
    ucontext_t *ctx;
    for (size_t i = 1; i < uthread_count; ++i) {
        ctx = &uthreads[i]->uctx;
        char* stack_from = ctx->uc_stack.ss_sp;
        char* stack_to = ctx->uc_stack.ss_sp + ctx->uc_stack.ss_size;

        if (stack_from <= (char*)&i && (char*)&i <= stack_to) {
            printf("thread_start: i=%ld thread_func: %p; arg: %p\n",
                i, uthreads[i]->thread_func, uthreads[i]->arg);
            uthreads[i]->thread_func(uthreads[i]->arg);
            schedule();
        }
    }
}

int uthread_create(uthread_t* thread, void (*start_routine), void *arg) {
    char* stack;
    uthread_t* new_ut;
    stack = create_stack(STACK_SIZE, &new_ut);
    getcontext(&new_ut->uctx);

    new_ut->uctx.uc_stack.ss_sp = stack;
    new_ut->uctx.uc_stack.ss_size = STACK_SIZE - sizeof(uthread_t);
    new_ut->uctx.uc_link = NULL;
    makecontext(&new_ut->uctx, thread_startup, 0);

    new_ut->thread_func = start_routine;
    new_ut->arg = arg;
    new_ut->finished = 0;
    uthreads[uthread_count] = new_ut;
    uthread_count++;
    thread = new_ut;
    return 0;
}

// void uthreads_run() {
//     swapcontext(&mainContext, &(uthreads[0]->uctx));

// }

void *mythread(void *arg) {
    char* str = (char *)arg;
    printf("mythread [%d %d %d]: Hello from mythread %s!\n", getpid(), getppid(), gettid(), str);

    for (size_t i = 0; i < 10; ++i) {
        printf("mythread: arg '%s' %p schedule())\n", str, &i);
        sleep(1);
        schedule();
    }
    printf("mythread: arg '%s' finished\n", str);
    return NULL;
}


int main() {
    uthread_t ut[3];
    char *arg[] = {"11111111", "222222222", "3333333"};

    uthread_t main_thread;
    uthreads[0] = &main_thread;
    uthread_count = 1;
    printf("main : %d %d %d\n", getpid(), getppid(), gettid());

    for (size_t i = 0; i < 3; ++i) {
        uthread_create(&ut[i], mythread, (void *)arg[i]);
    }
    // while (1) {
    //     uthreads_run();
    // }
    while(1) {
        schedule();
    }
    printf("main: finished\n");
}