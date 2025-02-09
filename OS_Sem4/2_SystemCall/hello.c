#include <sys/syscall.h>
#include <unistd.h>

#include "stdio.h"

int main() {
    // printf("Hello world");
    write(1, "Hello world!\n", 13);
    return 0;
}