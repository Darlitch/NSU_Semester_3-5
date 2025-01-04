#include "unistd.h"
#include "stdlib.h"
#include "stdio.h"
#include "errno.h"
#include "string.h"
#include "sys/stat.h"
#include "dirent.h"
#include "fcntl.h"

char* FindDirName(char* str, size_t* count) {
    size_t start = 0;
    size_t end = 0;
    size_t i = 0;
    while (str[i] != '\0') {
        if (str[i] == '/' && str[i+1] != '\0') {
            start = i+1;
            *count = 0;
        } else if (str[i] != '/') {
            (*count)++;
        }
        i++;
    }
    end = start + *count - 1;
    char* dirName = (char*)calloc(*count, sizeof(char));
    for (size_t i = start; i <= end; ++i) {
        dirName[end - i] = str[i];
    }
    return dirName;
}

char* TakeFileName(char* dirName, char* fileN, size_t lenDir) {
    char* fileName;
    const size_t lenFile = strlen(fileN);
    if (dirName[lenDir-1] == '/') {
        fileName = malloc(lenDir + lenFile + 1);
        memcpy(fileName, dirName, lenDir);
    } else {
        fileName = malloc(lenDir + lenFile + 2);
        memcpy(fileName, dirName, lenDir);
        fileName[lenDir] = '/';
        lenDir++;
    }
    memcpy(fileName + lenDir, fileN, lenFile + 1);
    return fileName;
}

int IsReg(char* fileName) {
    struct stat st = {0};
    stat(fileName, &st);
    if (S_ISREG(st.st_mode) == 1) {
        return 1;
    }
    return 0;
}

void ReverseFileName(char* fileName) {
    size_t len = strlen(fileName);
    size_t dot = 0;
    for (size_t i = len; i >= 0; --i) {
        if (fileName[i] == '.') {
            dot = i;
            break;
        }
    }
    for (size_t i = 0; i < dot/2; ++i) {
        char temp = fileName[i];
        fileName[i] = fileName[dot-i-1];
        fileName[dot-i-1] = temp;
    }
}

void CopyFile(char* fileName, char* newFileName) {
    int in, out, n;
    char buf;
    int fileSize;
    in = open(fileName, 0400);
    out = creat(newFileName, 0700);
    if (in < 0) {
        fprintf(stderr, "ERROR: Can`t open input file");
        exit(-4);
    }
    if (out < 0) {
        fprintf(stderr, "ERROR: Can`t create output file");
        exit(-5);
    }
    fileSize = lseek(in, (off_t) 0, SEEK_END);
    for (int i = fileSize - 1; i >= 0; --i) {
        lseek(in, (off_t) i, SEEK_SET);
        n = read(in, &buf, 1);
        if (n != 1) {
            fprintf(stderr, "ERROR: Can`t read byte");
            exit(-6);
        }
        n = write(out, &buf, 1);
        if (n != 1) {
            fprintf(stderr, "ERROR: Can`t write byte");
        }
    }
    close(in);
    close(out);
}

void WorkWithFiles(char* dirName, char* dirNameReverse) {
    DIR* dir;
    struct dirent* entry;
    struct stat st  = {0};
    dir = opendir(dirName);
    const size_t lenDir = strlen(dirName);
    const size_t lenDirReverse = strlen(dirNameReverse);
    if (dir != NULL) {
        while((entry = readdir(dir)) != NULL) {
            char* fullFileName = TakeFileName(dirName, entry->d_name, lenDir);
            if (IsReg(fullFileName)) {
                ReverseFileName(entry->d_name);
                char* newFileName = TakeFileName(dirNameReverse, entry->d_name,lenDirReverse);
                CopyFile(fullFileName, newFileName);
                free(newFileName);
            }
            free(fullFileName);
        }
    } else {
        fprintf(stderr, "ERROR: Couldn't open a directory!\n");
        exit(-3);
    }
}

int main(int argc, char** argv) {
    size_t dirSize = 0;
    struct stat st  = {0};
    char* dirName = FindDirName(argv[1], &dirSize);
    if (stat(argv[1], &st) == 0 && S_ISREG(st.st_mode) == 0) {
        if (mkdir(dirName, 0755) == -1) {
            fprintf(stderr, "ERROR: Couldn't create a directory!\n");
            exit(-2);
        }
        WorkWithFiles(argv[1], dirName);
    } else {
        fprintf(stderr, "ERROR: The directory does not exist!\n");
        exit(-1);
    }
    free(dirName);
    return 0;
}