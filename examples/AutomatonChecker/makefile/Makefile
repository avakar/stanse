CC=gcc
CFLAGS=-I/tmp -iquote -I. -lm
MAKEFLAGS += -rR

all: test.o
ifdef XYZ
	@touch $(XYZ)
endif

test.o: test.c FORCE

%.o: %.c
	$(CC) $(CFLAGS) -c -o $@ $<

clean:
	rm -f *.o

FORCE:

.PHONY: all clean FORCE
