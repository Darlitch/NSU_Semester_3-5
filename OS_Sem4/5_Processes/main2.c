#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>

int globalVar = 10;

int main() {
    int localVar = 20;
    pid_t pid;

    printf("GlobalVar: %p -- %d\n", &globalVar, globalVar);
    printf("LocalVar: %p -- %d\n", &localVar, localVar);
    printf("pid: %d\n\n", getpid());
    sleep(10);
    pid = fork();

    switch (pid)
    {
    case -1:
        perror("ERROR: fork failed");
        exit(1);
        break;
    case 0:
        printf("Child pid: %d\n", getpid());
        printf("Parent pid: %d\n", getppid());
        printf("GlobalVar: %p -- %d\n", &globalVar, globalVar);
        printf("LocalVar: %p -- %d\n", &localVar, localVar);
        globalVar = 30;
        localVar = 40;
        sleep(10);
        printf("Child globalVar after change: %d\n", globalVar);
        printf("Child localVar after change: %d\n\n", localVar);
        while(1) {
            printf("1\n");
            sleep(1);
        }
    default:
        printf("Child process did not exit normally\n");
        break;
    }
    return 0;
}