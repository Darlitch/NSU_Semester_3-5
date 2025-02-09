#include "routine.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "../list/list.h"

int asc_count = 0;
int asc_iter = 0;

int desc_count = 0;
int desc_iter = 0;

int eq_count = 0;
int eq_iter = 0;

int swap_count = 0;
int swap_iter = 0;

typedef enum compare_type {
    ASC, DESC, EQ
} compare_type_t;

void *compare_routine(void *args, compare_type_t type, int *iter, int *count) {
    const linked_list_t *ll = args;
    while (!ll->stop) {
        read_lock(&ll->first->lock);
        node_t *prev = ll->first;
        while (prev->next != NULL) {
            node_t *curr = prev->next;
            const int size = strnlen(prev->val, MAX_STRING_LENGTH);

            read_lock(&curr->lock);
            unlock(&prev->lock);

            switch (type) {
                case ASC:
                    if (size < strlen(curr->val)) {
                        (*count)++;
                    }
                    break;
                case DESC:
                    if (size > strlen(curr->val)) {
                        (*count)++;
                    }
                    break;
                case EQ:
                    if (size == strlen(curr->val)) {
                        (*count)++;
                    }
                    break;
            }
            prev = curr;
        }
        unlock(&prev->lock);
        (*iter)++;
    }
    return NULL;
}

void *asc_routine(void *args) {
    return compare_routine(args, ASC, &asc_iter, &asc_count);
}

void *desc_routine(void *args) {
    return compare_routine(args, DESC, &desc_iter, &desc_count);
}

void *eq_routine(void *args) {
    return compare_routine(args, EQ, &eq_iter, &eq_count);
}

void *swap_routine(void *args) {
    linked_list_t *ll = args;
    while(!ll->stop) {
        read_lock(&ll->first->lock);
        node_t *prev = ll->first;
        node_t *curr;

        while (prev->next != NULL) {
            if (rand() % 100 != 0) {
                curr = prev->next;

                read_lock(&curr->lock);
                unlock(&prev->lock);

                prev = curr;
                continue;
            }

            curr = prev->next;
            write_lock(&curr->lock);

            node_t *next = curr->next;
            if (next == NULL) {
                unlock(&curr->lock);
                break;
            }

            write_lock(&next->lock);
            prev->next = next;
            unlock(&prev->lock);
            curr->next = next->next;
            unlock(&curr->lock);
            next->next = curr;

            swap_count++;
            prev = next;

            unlock(&prev->lock);
            read_lock(&prev->lock);
        }

        unlock(&prev->lock);
        swap_iter++;
    }
    return NULL;
}

void *print_routine(void *args) {
    while (!((linked_list_t *) args)->stop) {
        sleep(1);
        printf("asc_count: %d, asc_iter: %d,\t\tdesc_count: %d, desc_iter: %d,\t\teq_count: %d, eq_iter: %d,\t\tswap_count: %d, swap_iter: %d\n",
            asc_count, asc_iter, desc_count, desc_iter, eq_count, eq_iter, swap_count, swap_iter);
    }
    return NULL;
}