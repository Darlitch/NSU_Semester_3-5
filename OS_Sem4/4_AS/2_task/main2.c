#include "stdio.h"
#include "unistd.h"
#include "stdlib.h"
#include "sys/mman.h"

void RecursiveStack() {
    int buf[4096];
    printf("pid: %d\n", getpid());
    usleep(100000);
    RecursiveStack();
}

void CycleHeap() {
    long size = 0;
    const int block = 1024*10;
    while(1) {
        int* a = malloc(block);
        size += block;
        printf("pid: %d; size %ld\n", getpid(), size);
        usleep(100000);
    }
}

void Mmap() {
    sleep(5);
    char* p;
    p = mmap(NULL, 4096*10, PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    printf("p = %p\n", p);
    sleep(5);

    mprotect(p, 4096, PROT_NONE);
    sleep(5);
    printf("p = %d\n", *p);
    // mprotect(p, 4096, PROT_READ);
    // *p = 1;
    munmap(p+4096*4, 4096*3);
    sleep(20);
}

int main() {
    printf("pid: %d\n", getpid());
    sleep(10);
    // RecursiveStack();
    // CycleHeap();
    Mmap();
    sleep(1000);
    return 0;
}