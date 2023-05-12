#ifndef te
#define te

#include <common.h>

void initDisasm(const char *triple);
void execDisasm(char *str, int size, uint64_t pc, uint8_t *code, int nbyte);

#endif
