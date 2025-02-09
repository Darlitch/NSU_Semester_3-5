#include "stdio.h"
#include "unistd.h"
#include "stdlib.h"
#include "string.h"

int c = 5;
char d = 'a';

int e;
char g;

const int m;
const char n;

void LocalVar() {
    int la;
    char lb;
    printf("LocalVar adress: la - %p, lb - %p\n", &la, &lb);
}

void StaticVar() {
    static int sa;
    static char sb;
    printf("StaticVar adress: sa - %p, sb - %p\n", &sa, &sb);
}

void ConstVar() {
    const int ca;
    const char cb;
    printf("ConstVar adress: ca - %p, cb - %p\n", &ca, &cb);
}

void GlobalVar() {
    printf("GlobalInitVar adress: c - %p, d - %p\n", &c, &d);
    printf("GlobalNonInitVar adress: e - %p, g - %p\n", &e, &g);
    printf("ConstGlobalVar adress: m - %p, n - %p\n", &m, &n);
}

void InitLocalVar() {
    int ila;
    ila = 5;
    // return &ila;
    printf("InitLocalVar adress: ila - %p\n", &ila);
}

void HeapBufffer() {
    char* buf = (char*)malloc(100);
    char* buf2 = "hello world!";
    memcpy(buf, buf2, 12);
    for (size_t i = 0; i < 100; ++i) {
        printf("%c", buf[i]);
    }
    printf("\n");
    free(buf);
    // int temp = 100;
    // while(1) {
    //     temp+=100;
    //     printf("buf: %p\n");
    // }
    printf("free:\n");
    for (size_t i = 0; i < 100; ++i) {
        printf("%c", buf[i]);
    }
    printf("\n");

    buf = (char*)malloc(100);
    memcpy(buf, buf2, 12);
    for (size_t i = 0; i < 100; ++i) {
        printf("%c", buf[i]);
    }
    printf("\n");
//     char *buf3 = buf+6;
    // free(buf + 6);
    // printf("free2:\n");
    // for (size_t i = 0; i < 100; ++i) {
    //     printf("%c", buf[i]);
    // }
    // printf("\n");
}

void EnvVar() {
    char* vtuber = getenv("vtuber");
    printf("vtuber = %s\n", vtuber);
    *vtuber = 'N';
    *(vtuber+1) = 'O';
    printf("vtuber = %s\n", vtuber);
}

int main() {
    printf("pid: %d\n", getpid());
    LocalVar();
    // getchar();
    StaticVar();
    // getchar();
    ConstVar();
    // getchar();
    GlobalVar();
    // getchar();
    InitLocalVar();
    printf("\n");
    // getchar();
    HeapBufffer();
    printf("\n");
    // getchar();
    EnvVar();
    getchar();
    // int* ila = InitLocalVar();
    // printf("InitLocalVar adress: ila - %p\n", ila);
}