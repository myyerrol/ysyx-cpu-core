#ifndef __GPR_H__
#define __GPR_H__

#include <common.h>

word_t getISAGPR(int id);
word_t getISAGPRData(const char *gpr, bool *success);
const char *getISAGPRName(int id);
void printfISAGPRData();

#endif
