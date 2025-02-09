#include "stdio.h"
#include "unistd.h"

int main(int argc, char** argv) {
    printf("pid: %d\n", getpid());
    sleep(1);
    // execl("./prog1", "prog", NULL);
    printf("Hello World!");
    return 0;
}