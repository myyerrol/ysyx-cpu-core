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

#include "mmu.h"
#include "sim.h"
#include "../../include/common.h"
#include <difftest-def.h>

#ifdef CONFIG_ISA_riscv32
#undef DEFAULT_ISA
#define DEFAULT_ISA "RV32IM"
#endif

static std::vector<std::pair<reg_t, abstract_device_t*>> difftest_plugin_devices;
static std::vector<std::string> difftest_htif_args;
static std::vector<std::pair<reg_t, mem_t*>> difftest_mem(
    1, std::make_pair(reg_t(DRAM_BASE), new mem_t(CONFIG_MSIZE)));
static std::vector<int> difftest_hartids;
static debug_module_config_t difftest_dm_config = {
  .progbufsize = 2,
  .max_sba_data_width = 0,
  .require_authentication = false,
  .abstract_rti = 0,
  .support_hasel = true,
  .support_abstract_csr_access = true,
  .support_abstract_fpr_access = true,
  .support_haltgroups = true,
  .support_impebreak = true
};

#define CSR_ARR_LEN 2048

struct diff_context_t {
  word_t gpr[32];
  word_t csr[CSR_ARR_LEN];
  word_t pc;
};

static sim_t* s = NULL;
static processor_t *p = NULL;
static state_t *state = NULL;

void sim_t::diff_init(int port) {
  p = get_core("0");
  state = p->get_state();
  p->put_csr(0x300, 0xa00001800);
}

void sim_t::diff_step(uint64_t n) {
  step(n);
}

void sim_t::diff_get_regs(void* diff_context) {
  struct diff_context_t* ctx = (struct diff_context_t*)diff_context;
  ctx->pc = state->pc;
  for (int i = 0; i < NXPR; i++) {
    ctx->gpr[i] = state->XPR[i];
  }
  for (int i = 0; i < CSR_ARR_LEN; i++) {
    if (i == 0x300) { ctx->csr[i] = p->get_csr(i); } // MSTATUS
    if (i == 0x305) { ctx->csr[i] = p->get_csr(i); } // MTVEC
    if (i == 0x341) { ctx->csr[i] = p->get_csr(i); } // MEPC
    if (i == 0x342) { ctx->csr[i] = p->get_csr(i); } // MCAUSE
  }
}

void sim_t::diff_set_regs(void* diff_context) {
  struct diff_context_t* ctx = (struct diff_context_t*)diff_context;
  state->pc = ctx->pc;
  for (int i = 0; i < NXPR; i++) {
    state->XPR.write(i, (sword_t)ctx->gpr[i]);
  }
  for (int i = 0; i < CSR_ARR_LEN; i++) {
    if (i == 0x300) { p->put_csr(i, ctx->csr[i]); } // MSTATUS
    if (i == 0x305) { p->put_csr(i, ctx->csr[i]); } // MTVEC
    if (i == 0x341) { p->put_csr(i, ctx->csr[i]); } // MEPC
    if (i == 0x342) { p->put_csr(i, ctx->csr[i]); } // MCAUSE
  }
}

void sim_t::diff_memcpy(reg_t dest, void* src, size_t n) {
  mmu_t* mmu = p->get_mmu();
  for (size_t i = 0; i < n; i++) {
    mmu->store<uint8_t>(dest+i, *((uint8_t*)src+i));
  }
}

extern "C" {

void difftest_memcpy(paddr_t addr, void *buf, size_t n, bool direction) {
  if (direction == DIFFTEST_TO_REF) {
    s->diff_memcpy(addr, buf, n);
  } else {
    assert(0);
  }
}

void difftest_regcpy(void* dut, bool direction) {
  if (direction == DIFFTEST_TO_REF) {
    s->diff_set_regs(dut);
  } else {
    s->diff_get_regs(dut);
  }
}

void difftest_exec(uint64_t n) {
  s->diff_step(n);
}

void difftest_init(int port) {
  difftest_htif_args.push_back("");
  cfg_t cfg(/*default_initrd_bounds=*/std::make_pair((reg_t)0, (reg_t)0),
            /*default_bootargs=*/nullptr,
            /*default_isa=*/DEFAULT_ISA,
            /*default_priv=*/DEFAULT_PRIV,
            /*default_varch=*/DEFAULT_VARCH,
            /*default_misaligned=*/false,
            /*default_endianness*/endianness_little,
            /*default_pmpregions=*/16,
            /*default_mem_layout=*/std::vector<mem_cfg_t>(),
            /*default_hartids=*/std::vector<size_t>(1),
            /*default_real_time_clint=*/false,
            /*default_trigger_count=*/4);
  s = new sim_t(&cfg, false,
      difftest_mem, difftest_plugin_devices, difftest_htif_args,
      difftest_dm_config, nullptr, false, NULL,
      false,
      NULL,
      true);
  s->diff_init(port);
}

void difftest_raise_intr(uint64_t NO) {
  trap_t t(NO);
  p->take_trap_public(t, state->pc);
}

}
