#ifndef __ISA_H__
#define __ISA_H__

#include <common.h>

typedef struct {
    word_t gpr[32];
    word_t pc;
} CPUState;

extern CPUState cpu;

void initISA();

#endif
