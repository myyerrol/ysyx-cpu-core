#include <am.h>
#include <klib-macros.h>
#include <klib.h>

#include ISA_H

#define SERIAL_PORT 0xa00003f8

extern char _heap_start;
int main(const char *args);

extern char _pmem_start;
#define PMEM_SIZE (128 * 1024 * 1024)
#define PMEM_END  ((uintptr_t)&_pmem_start + PMEM_SIZE)

Area heap = RANGE(&_heap_start, PMEM_END);
#ifndef MAINARGS
#define MAINARGS ""
#endif
static const char mainargs[] = MAINARGS;

#define NPC_TRAP(code) asm volatile("mv a0, %0; ebreak" : :"r"(code))

void putch(char ch) {
  outb(SERIAL_PORT, ch);
}

void halt(int code) {
  NPC_TRAP(code);

  while (1);
}

void _trm_init() {
  int ret = main(mainargs);
  halt(ret);
}
