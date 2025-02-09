#ifndef LOCK_H_
#define LOCK_H_

#include <pthread.h>

typedef struct custom_lock {
    #ifdef USE_SPINLOCK
        pthread_spinlock_t lock;
    #elifdef USE_MUTEX
        pthread_mutex_t lock;
    #elifdef USE_RWLOCK
        pthread_rwlock_t lock;
    #endif
} custom_lock_t;

void lock_init(custom_lock_t *lock);

void unlock(custom_lock_t *lock);

void write_lock(custom_lock_t *lock);

void read_lock(custom_lock_t *lock);

#endif
