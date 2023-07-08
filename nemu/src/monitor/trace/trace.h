#ifndef __TRACE_H__
#define __TRACE_H__

#include <elf.h>

#include <common.h>
#include <cpu/decode.h>
#include <memory/paddr.h>

void itrace_record(char *logbuf);
void itrace_display(char *type,
                    int inst_num,
                    char *op,
                    Decode *s,
                    int rd,
                    int rs1,
                    int rs2,
                    word_t src1,
                    word_t src2,
                    word_t imm,
                    word_t rd_val);

void mtrace_display(char *type,
                    char *dir,
                    word_t addr,
                    word_t data,
                    word_t len);

void ftrace_init(const char *elf_file);
void ftrace_display(char *type,
                    bool inst_func_call,
                    bool inst_func_ret,
                    word_t pc,
                    word_t dpnc);
void ftrace_free();
void dtrace_display(char *type,
                    char *dir,
                    const char *name,
                    word_t addr,
                    word_t data);

#endif
