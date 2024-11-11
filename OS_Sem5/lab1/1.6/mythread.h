#ifndef MYTHREAD_H_
#define MYTHREAD_H_

#include "mythread_t.h"

int mypthread_create(mythread_t *thread, void *(*start_routine)(void *), void *arg);
void mypthread_join(mythread_t mytid, void **retval);

#endif