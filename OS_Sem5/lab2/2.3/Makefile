TARGET1 = spinlock
TARGET2 = mutex
TARGET3 = rwlock
MACR1 = -DUSE_SPINLOCK
MACR2 = -DUSE_MUTEX
MACR3 = -DUSE_RWLOCK

SRCS = list/list.c lock/lock.c routine/routine.c main.c

CC=gcc
RM=rm
CFLAGS= -g -Wall
LIBS=-lpthread
INCLUDE_DIR="."

all: clean ${TARGET1} ${TARGET2} ${TARGET3}

${TARGET1}: ${SRCS}
	${CC} ${CFLAGS} ${MACR1} -I${INCLUDE_DIR} ${SRCS} ${LIBS} -o ${TARGET1}

${TARGET2}: ${SRCS}
	${CC} ${CFLAGS} ${MACR2} -I${INCLUDE_DIR} ${SRCS} ${LIBS} -o ${TARGET2}

${TARGET3}: ${SRCS}
	${CC} ${CFLAGS} ${MACR3} -I${INCLUDE_DIR} ${SRCS} ${LIBS} -o ${TARGET3}

clean:
	${RM} -f *.o ${TARGET1} ${TARGET2} ${TARGET3}
