#ifndef __DEBUG_DIFFTEST_H__
#define __DEBUG_DIFFTEST_H__

#include <common.h>

enum { DIFFTEST_TO_DUT, DIFFTEST_TO_REF };

#if defined(CONFIG_ISA_x86)
    #define DIFFTEST_REG_SIZE (sizeof(uint32_t) * 9)
#elif defined(CONFIG_ISA_mips32)
    #define DIFFTEST_REG_SIZE (sizeof(uint32_t) * 38)
#elif defined(CONFIG_ISA_riscv32)
    #define DIFFTEST_REG_SIZE (sizeof(uint32_t) * 33)
#elif defined(CONFIG_ISA_riscv64)
    #define DIFFTEST_REG_SIZE (sizeof(uint64_t) * 33)
#else
    #error Unsupport ISA
#endif

void initDebugDifftest(char *ref_so_file, long img_size, int port);
void skipDebugDifftestRef();
void skipDebugDifftestDut(int ref_num, int dut_num);
void stepDebugDifftest(vaddr_t pc, vaddr_t npc);

#endif
