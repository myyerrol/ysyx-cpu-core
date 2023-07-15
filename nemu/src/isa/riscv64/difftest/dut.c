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
  if (cpu.pc != ref_r->pc) {
    return false;
  }

  for (int i = 0; i < 32; i++) {
    if (gpr(i) != ref_r->gpr[i]) {
      return false;
    }
  }

  int CSR_CODE_ARR[] = { 0x300, 0x305, 0x341, 0x342 };
  for (int i = 0; i < ARRLEN(CSR_CODE_ARR); i++) {
    if(cpu.csr[CSR_CODE_ARR[i]] != ref_r->csr[CSR_CODE_ARR[i]]) {
      return false;
    }
  }

  return true;
}

void isa_difftest_display(CPU_state *ref_r, vaddr_t pc) {
  char *space_num = "";
  char *space_reg = "";
  char *error_str = "";

  error_str = (cpu.pc != ref_r->pc) ? ANSI_FMT("*", ANSI_FG_RED) : "";
  _Log("[difftest] pc dut val: " FMT_WORD " ref val: " FMT_WORD "%s\n\n",
         cpu.pc,
         ref_r->pc,
         error_str);

  for (int i = 0; i < 32; i++) {
    error_str = (gpr(i) != ref_r->gpr[i]) ? ANSI_FMT("*", ANSI_FG_RED) : "";

    const char *name = reg_name(i, 0);
    space_num = (i < 10) ? " " : "";
    space_reg = (strcmp(name, "s10") != 0 &&
                 strcmp(name, "s11") != 0) ? " " : "";

    _Log("[difftest] gpr i: %d%s dut val: %s%s = " FMT_WORD
                               " ref val: %s%s = " FMT_WORD "%s\n",
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

  printf("\n");
  int   CSR_CODE_ARR[] = { 0x300, 0x305, 0x341, 0x342 };
  char *CSR_NAME_ARR[] = { "mstatus", "mtvec  ", "mepc   ", "mcause " };
  for (int i = 0; i < ARRLEN(CSR_CODE_ARR); i++) {
    error_str = (cpu.csr[CSR_CODE_ARR[i]] != ref_r->csr[CSR_CODE_ARR[i]]) ?
                 ANSI_FMT("*", ANSI_FG_RED) : "";
    printf("[difftest] csr %s dut val: " FMT_WORD
                            " ref val: " FMT_WORD "%s\n",
            CSR_NAME_ARR[i],
            cpu.csr[CSR_CODE_ARR[i]],
            ref_r->csr[CSR_CODE_ARR[i]],
            error_str);
  }
}

void isa_difftest_attach() {
}
