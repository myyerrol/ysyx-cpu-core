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

word_t isa_raise_intr(word_t NO, vaddr_t epc) {
  /* TODO: Trigger an interrupt/exception with ``NO''.
   * Then return the address of the interrupt/exception vector.
   */
  cpu.csr[CSR_MEPC] = epc;
  cpu.csr[CSR_MCAUSE] = NO;

  word_t mstatus = cpu.csr[CSR_MSTATUS];
  cpu.csr[CSR_MSTATUS] = (SEXT(BITS(mstatus, 63, 13), 51) << 13) |
                              (BITS(3, 1, 0) << 11) |
                              (BITS(mstatus, 10, 8) << 8) |
                              (BITS(mstatus, 3, 3) << 7) |
                              (BITS(mstatus, 6, 4) << 4) |
                              (BITS(0, 1, 1) << 3) |
                              (BITS(mstatus, 2, 0)) ;

#ifdef CONFIG_ETRACE_COND_PROCESS
  printf("[etrace] mcause: " FMT_WORD \
                ", mstatus: " FMT_WORD \
                ", mepc: " FMT_WORD \
                ", mtvec: " FMT_WORD "\n",
          cpu.csr[CSR_MCAUSE],
          cpu.csr[CSR_MSTATUS],
          cpu.csr[CSR_MEPC],
          cpu.csr[CSR_MTVEC]);
  printf("[etrace] ecall before: " FMT_WORD ", after: " FMT_WORD "\n",
           mstatus, cpu.csr[CSR_MSTATUS]);
#endif

  return cpu.csr[CSR_MTVEC];
}

word_t isa_mret() {
  word_t mstatus = cpu.csr[CSR_MSTATUS];
  cpu.csr[CSR_MSTATUS] = (SEXT(BITS(mstatus, 63, 13), 51) << 13) |
                              (BITS(0, 1, 0) << 11) |
                              (BITS(mstatus, 10, 8) << 8) |
                              (BITS(1, 0, 0) << 7)  |
                              (BITS(mstatus, 6, 4) << 4) |
                              (BITS(mstatus, 7, 7) << 3) |
                              (BITS(mstatus, 2, 0)) ;
#ifdef CONFIG_ETRACE_COND_PROCESS
  printf("[etrace] mret  before: " FMT_WORD ", after: " FMT_WORD "\n\n",
           mstatus, cpu.csr[CSR_MSTATUS]);
#endif
  return cpu.csr[CSR_MEPC];
}

word_t isa_query_intr() {
  return INTR_EMPTY;
}
