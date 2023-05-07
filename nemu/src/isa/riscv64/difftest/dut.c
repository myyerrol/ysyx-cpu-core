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
#include <cpu/difftest.h>
#include "../local-include/reg.h"

bool isa_difftest_checkregs(CPU_state *ref_r, vaddr_t pc) {
  for (int i = 0; i < 32; i++) {
    if (ref_r->gpr[i] != gpr(i)) {
      printf("[difftest] error at pc: " FMT_WORD "\n", pc);
      return false;
    }
  }
  return true;
}

void isa_difftest_display(CPU_state *ref_r, vaddr_t pc) {
  char *space_num = "";
  char *space_reg = "";
  char *error_str = "";
  for (int i = 0; i < 32; i++) {
    if (ref_r->gpr[i] != gpr(i)) {
      error_str = ANSI_FMT("*", ANSI_FG_RED);
    }
    else {
      error_str = "";
    }
    const char *name = reg_name(i, 0);
    space_num = (i < 10) ? " " : "";
    space_reg = (strcmp(name, "s10") != 0 && strcmp(name, "s11") != 0) ? " " : "";

    printf("[difftest] reg i: %d%s dut val: %s%s = " FMT_WORD " ref val: %s%s = " FMT_WORD "%s\n",
           i,
           space_num,
           space_reg,
           name,
           gpr(i),
           space_reg,
           name,
           ref_r->gpr[i],
           error_str);
  }
}

void isa_difftest_attach() {
}
