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
#include <memory/paddr.h>
#include <trace.h>

word_t vaddr_ifetch(vaddr_t addr, int len) {
  word_t data = paddr_read(addr, len);
#ifdef CONFIG_MTRACE_COND_PROCESS
  mtrace_display("process", "rd ins", addr, data, 0);
#endif
  return data;
}

word_t vaddr_read(vaddr_t addr, int len) {
  word_t data = paddr_read(addr, len);
#ifdef CONFIG_MTRACE_COND_PROCESS
  mtrace_display("process", "rd mem", addr, data, 0);
#endif
  return data;
}

void vaddr_write(vaddr_t addr, int len, word_t data) {
#ifdef CONFIG_MTRACE_COND_PROCESS
  mtrace_display("process", "wr mem", addr, data, 0);
#endif
  paddr_write(addr, len, data);
}
