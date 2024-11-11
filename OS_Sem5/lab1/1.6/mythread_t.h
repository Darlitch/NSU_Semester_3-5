#ifndef MYTHREAD_T_H_
#define MYTHREAD_T_H_

// #include <stdio.h>

typedef void *(*start_routine_t)(void *);

typedef struct _mythread {
    int mytid;
    void *arg;
    start_routine_t start_routine;
    void *retval;
    volatile int joined;
    volatile int finished;

} mythread_struct_t;

typedef mythread_struct_t *mythread_t;

#endif