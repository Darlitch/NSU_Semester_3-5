#ifndef UTHREAD_T_H_
#define UTHREAD_T_H_

#include <ucontext.h>

typedef struct uthread {
    void (*thread_func)(void *);
    void *arg;
    int finished;
    ucontext_t uctx;
} uthread_t;

#endif