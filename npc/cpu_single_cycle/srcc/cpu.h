#ifndef __CPU_EXEC_H__
#define __CPU_EXEC_H__

#include <common.h>
#include "state.h"

void cpu_exec(uint64_t n);

void set_npc_state(int state, vaddr_t pc, int halt_ret);

#define NPC_INST_TRAP(thispc, code) set_npc_state(NPC_END, thispc, code)

#endif
