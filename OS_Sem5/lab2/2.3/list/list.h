#ifndef LIST_H_
#define LIST_H_

#include "../lock/lock.h"
#include "stdbool.h"

#define MAX_STRING_LENGTH 100
#define LIST_SIZE 100000

typedef struct node_t_ {
    struct node_t_ *next;
    custom_lock_t lock;
    char val[MAX_STRING_LENGTH];
} node_t;

typedef struct linked_list_t_ {
    node_t *first;
    bool stop;
} linked_list_t;

node_t *create_node(const char *val);

void linked_list_destroy(linked_list_t *ll);

#endif