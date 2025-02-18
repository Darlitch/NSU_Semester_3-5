#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "list/list.h"
#include "routine/routine.h"

int main() {
    linked_list_t *ll = malloc(sizeof(linked_list_t));
    ll->stop = false;
    srand(time(NULL));
    char val[MAX_STRING_LENGTH] = {0};
    memset(val, '0', rand() % (MAX_STRING_LENGTH - 1));
    ll->first = create_node(val);

    node_t *last = ll->first;
    for (int i = 1; i < LIST_SIZE; ++i) {
        memset(val, 0, MAX_STRING_LENGTH);
        memset(val, '0', rand() % (MAX_STRING_LENGTH - 1));
        node_t *new = create_node(val);
        last->next = new;
        last = new;
    }

    pthread_t tid[7];
    void* (*routines[7])(void*) = {
        asc_routine,
        desc_routine,
        eq_routine,
        print_routine,
        swap_routine,
        swap_routine,
        swap_routine
    };

    for (int i = 0; i < 7; ++i) {
        if (pthread_create(&tid[i], NULL, routines[i], ll)) {
            printf("main: pthread_create()\n");
        }
    }

    sleep(10);

    ll->stop = true;
    for (int i = 0; i < 7; ++i) {
        pthread_join(tid[i], NULL);
    }

    linked_list_destroy(ll);
    return 0;
}
