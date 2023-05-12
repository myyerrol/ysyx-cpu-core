#include <state.h>

NPCState npc_state = { .state = NPC_STOP };

int judgeNPCStateIsBad() {
    int good = (npc_state.state == NPC_END && npc_state.halt_ret == 0) ||
               (npc_state.state == NPC_QUIT);
    return !good;
}

void setNPCState(int state, vaddr_t pc, int ret) {
    npc_state.state    = state;
    npc_state.halt_pc  = pc;
    npc_state.halt_ret = ret;
}
