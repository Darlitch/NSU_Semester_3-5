#include "lock.h"

void lock_init(custom_lock_t *lock) {
#ifdef USE_SPINLOCK
    pthread_spin_init(&lock->lock, 0);
#elifdef USE_MUTEX
    pthread_mutex_init(&lock->lock, NULL);
#elifdef USE_RWLOCK
    pthread_rwlock_init(&lock->lock, NULL);
#endif
}

void unlock(custom_lock_t *lock) {
#ifdef USE_SPINLOCK
    pthread_spin_unlock(&lock->lock);
#elifdef USE_MUTEX
    pthread_mutex_unlock(&lock->lock);
#elifdef USE_RWLOCK
    pthread_rwlock_unlock(&lock->lock);
#endif
}

void write_lock(custom_lock_t *lock) {
#ifdef USE_SPINLOCK
    pthread_spin_lock(&lock->lock);
#elifdef USE_MUTEX
    pthread_mutex_lock(&lock->lock);
#elifdef USE_RWLOCK
    pthread_rwlock_wrlock(&lock->lock);
#endif
}

void read_lock(custom_lock_t *lock) {
#ifdef USE_SPINLOCK
    pthread_spin_lock(&lock->lock);
#elifdef USE_MUTEX
    pthread_mutex_lock(&lock->lock);
#elifdef USE_RWLOCK
    pthread_rwlock_rdlock(&lock->lock);
#endif
}