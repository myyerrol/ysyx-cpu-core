#ifndef __CPU_H__
#define __CPU_H__

#include <common.h>

void execCPU(uint64_t num);

// #define NPC_INST_TRAP(thispc, code) set_npc_state(NPC_END, thispc, code)

#endif
