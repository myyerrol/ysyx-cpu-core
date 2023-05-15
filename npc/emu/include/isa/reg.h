#ifndef __REG_H__
#define __REG_H__

#include <common.h>

word_t getISAReg(int id);
word_t getISARegData(const char *reg, bool *success);
const char *getISARegName(int id);
void printfISARegData();

#endif
