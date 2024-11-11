TARGET = thread-example
SRCS = 1_6.c

CC=gcc
RM=rm
CFLAGS= -g -Wall
LIBS=-lpthread -lmythread
INCLUDE_DIR="."

all: clean ${TARGET}

${TARGET}: ${SRCS}
	${CC} ${CFLAGS} -I${INCLUDE_DIR} ${SRCS} ${LIBS} -o ${TARGET}

clean:
	${RM} -f *.o ${TARGET}
