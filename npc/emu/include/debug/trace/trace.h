#ifndef __TRACE_H__
#define __TRACE_H__

#include <common.h>

void recordDebugITrace(char *logbuf);
void printfDebugITrace();

void printfDebugMTrace(char *type,
                       char *dir,
                       word_t addr,
                       word_t data,
                       word_t len);

void printfDebugFTrace(char *type,
                       bool inst_func_call,
                       bool inst_func_ret,
                       word_t pc,
                       word_t dpnc);
void freeDebugFTrace();

void printfDebugDTrace(char *type,
                       char *dir,
                       const char *name,
                       word_t addr,
                       word_t data);

void initDebugTrace(char *elf_file);

#endif
