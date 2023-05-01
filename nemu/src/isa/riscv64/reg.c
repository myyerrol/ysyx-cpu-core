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

#include <isa.h>
#include "local-include/reg.h"

const char *regs[] = {
  "$0", "ra", "sp",  "gp",  "tp", "t0", "t1", "t2",
  "s0", "s1", "a0",  "a1",  "a2", "a3", "a4", "a5",
  "a6", "a7", "s2",  "s3",  "s4", "s5", "s6", "s7",
  "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
};

void isa_reg_display() {
  char *space_num = " ";
  char *space_reg = " ";
  char *flag  = "";
  for (int i = 0; i < ARRLEN(regs); i++) {
    if (i < 10) {
      space_num = " ";
    }
    else {
      space_num = "";
    }

    if (strcmp(regs[i], "s10") == 0 || strcmp(regs[i], "s11") == 0) {
      space_reg = "";
    }
    else {
      space_reg = " ";
    }

    word_t val = cpu.gpr[i];
    if (val != 0) {
      flag = ANSI_FMT("*", ANSI_FG_GREEN);
    }
    else {
      flag = "";
    }

    printf("[reg] i: %d%s name: %s%s = " FMT_WORD "%s\n",
           i,
           space_num,
           space_reg,
           regs[i],
           val,
           flag);
  }
}

word_t isa_reg_str2val(const char *s, bool *success) {
  word_t val = 0;

  for (int i = 0; i < ARRLEN(regs); i++) {
    if (strcmp(regs[i], s) == 0) {
      val = cpu.gpr[i];
      *success = true;
      break;
    }
  }

  return val;
}
