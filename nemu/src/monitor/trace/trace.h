#ifndef __TRACE_H__
#define __TRACE_H__

#include <common.h>
#include <memory/paddr.h>
#include <elf.h>

void itrace_record(char *logbuf);
void itrace_display();
void mtrace_display(char *type,
                    char *dir,
                    word_t addr,
                    word_t data,
                    word_t len);
void ftrace_init(const char *elf_file);
void ftrace_display(char *type,
                    bool *inst_func_call,
                    bool *inst_func_ret,
                    word_t pc,
                    word_t dpnc);
void ftrace_free();
void dtrace_display(char *type,
                    char *dir,
                    const char *name,
                    word_t addr,
                    word_t data);

#endif
