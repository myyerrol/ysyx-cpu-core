/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#ifndef __UTILS_H__
#define __UTILS_H__

#include <common.h>

// state
enum { NEMU_RUNNING, NEMU_STOP, NEMU_END, NEMU_ABORT, NEMU_QUIT };

typedef struct {
  int state;
  vaddr_t halt_pc;
  uint32_t halt_ret;
} NEMUState;

extern NEMUState nemu_state;

// timer
uint64_t get_time();

// log
#define ANSI_FG_BLACK   "\33[1;30m"
#define ANSI_FG_RED     "\33[1;31m"
#define ANSI_FG_GREEN   "\33[1;32m"
#define ANSI_FG_YELLOW  "\33[1;33m"
#define ANSI_FG_BLUE    "\33[1;34m"
#define ANSI_FG_MAGENTA "\33[1;35m"
#define ANSI_FG_CYAN    "\33[1;36m"
#define ANSI_FG_WHITE   "\33[1;37m"
#define ANSI_BG_BLACK   "\33[1;40m"
#define ANSI_BG_RED     "\33[1;41m"
#define ANSI_BG_GREEN   "\33[1;42m"
#define ANSI_BG_YELLOW  "\33[1;43m"
#define ANSI_BG_BLUE    "\33[1;44m"
#define ANSI_BG_MAGENTA "\33[1;35m"
#define ANSI_BG_CYAN    "\33[1;46m"
#define ANSI_BG_WHITE   "\33[1;47m"
#define ANSI_NONE       "\33[0m"

#define ANSI_FMT(str, fmt) fmt str ANSI_NONE

#define log_write(...) IFDEF(CONFIG_TARGET_NATIVE_ELF, \
  do { \
    extern FILE* log_fp; \
    extern bool log_enable(); \
    if (log_enable()) { \
      fprintf(log_fp, __VA_ARGS__); \
      fflush(log_fp); \
    } \
  } while (0) \
)

#define _Log(...) \
  do { \
    printf(__VA_ARGS__); \
    log_write(__VA_ARGS__); \
  } while (0)
#endif

// utils
char *strrpc(char *str, char *str_old, char *str_new);

#define PRINTF_BIN_PATTERN_INT2 "%c%c"
#define PRINTF_BIN_PATTERN_INT3 "%c%c%c "
#define PRINTF_BIN_PATTERN_INT5 "%c%c%c%c%c "
#define PRINTF_BIN_PATTERN_INT7 "%c%c%c%c%c%c%c "
#define PRINTF_BIN_PATTERN_INT8 "%c%c%c%c%c%c%c%c "
#define PRINTF_BIN_PATTERN_INT16 \
  PRINTF_BIN_PATTERN_INT8 PRINTF_BIN_PATTERN_INT8
#define PRINTF_BIN_PATTERN_INT32 \
  PRINTF_BIN_PATTERN_INT16 PRINTF_BIN_PATTERN_INT16
#define PRINTF_BIN_PATTERN_INT64 \
  PRINTF_BIN_PATTERN_INT32 PRINTF_BIN_PATTERN_INT32
#define PRINTF_BIN_PATTERN_INST \
  PRINTF_BIN_PATTERN_INT7 PRINTF_BIN_PATTERN_INT5 PRINTF_BIN_PATTERN_INT5 \
  PRINTF_BIN_PATTERN_INT3 PRINTF_BIN_PATTERN_INT5 PRINTF_BIN_PATTERN_INT5 \
  PRINTF_BIN_PATTERN_INT2
#define PRINTF_BIN_INT2(i)      \
  (((i) & 0x02ll) ? '1' : '0'), \
  (((i) & 0x01ll) ? '1' : '0')
#define PRINTF_BIN_INT3(i)      \
  (((i) & 0x04ll) ? '1' : '0'), \
  (((i) & 0x02ll) ? '1' : '0'), \
  (((i) & 0x01ll) ? '1' : '0')
#define PRINTF_BIN_INT5(i)      \
  (((i) & 0x10ll) ? '1' : '0'), \
  (((i) & 0x08ll) ? '1' : '0'), \
  (((i) & 0x04ll) ? '1' : '0'), \
  (((i) & 0x02ll) ? '1' : '0'), \
  (((i) & 0x01ll) ? '1' : '0')
#define PRINTF_BIN_INT7(i)      \
  (((i) & 0x40ll) ? '1' : '0'), \
  (((i) & 0x20ll) ? '1' : '0'), \
  (((i) & 0x10ll) ? '1' : '0'), \
  (((i) & 0x08ll) ? '1' : '0'), \
  (((i) & 0x04ll) ? '1' : '0'), \
  (((i) & 0x02ll) ? '1' : '0'), \
  (((i) & 0x01ll) ? '1' : '0')
#define PRINTF_BIN_INT8(i)      \
  (((i) & 0x80ll) ? '1' : '0'), \
  (((i) & 0x40ll) ? '1' : '0'), \
  (((i) & 0x20ll) ? '1' : '0'), \
  (((i) & 0x10ll) ? '1' : '0'), \
  (((i) & 0x08ll) ? '1' : '0'), \
  (((i) & 0x04ll) ? '1' : '0'), \
  (((i) & 0x02ll) ? '1' : '0'), \
  (((i) & 0x01ll) ? '1' : '0')
#define PRINTF_BIN_INT16(i) \
  PRINTF_BIN_INT8((i) >> 8), PRINTF_BIN_INT8(i)
#define PRINTF_BIN_INT32(i) \
  PRINTF_BIN_INT16((i) >> 16), PRINTF_BIN_INT16(i)
#define PRINTF_BIN_INT64(i) \
  PRINTF_BIN_INT32((i) >> 32), PRINTF_BIN_INT32(i)
#define PRINTF_BIN_INST(i) \
  PRINTF_BIN_INT7((i) >> 25), PRINTF_BIN_INT5((i) >> 20), \
  PRINTF_BIN_INT5((i) >> 15), PRINTF_BIN_INT3((i) >> 12), \
  PRINTF_BIN_INT5((i) >> 7),  PRINTF_BIN_INT5((i) >> 2),  \
  PRINTF_BIN_INT2(i)
