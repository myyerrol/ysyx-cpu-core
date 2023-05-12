#ifndef __HOST_H__
#define __HOST_H__

#include <common.h>

word_t readMemoryHost(void *addr, int len);
void writeMemoryHost(void *addr, int len, word_t data);

#endif
