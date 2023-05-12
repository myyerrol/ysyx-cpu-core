#ifndef __ISA_H__
#define __ISA_H__

#include <common.h>

word_t getISARegData(const char *reg, bool *success);
const char *getISARegName(int id);
void printfISARegData();

#endif
