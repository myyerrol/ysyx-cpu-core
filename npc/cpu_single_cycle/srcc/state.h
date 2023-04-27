#ifndef __STATE_H__
#define __STATE_H__

#include <common.h>

enum { NPC_RUNNING, NPC_STOP, NPC_END, NPC_ABORT, NPC_QUIT };

typedef struct {
    int state;
    vaddr_t halt_pc;
    uint32_t halt_ret;
} NPCState;

extern NPCState npc_state;

int is_exit_status_bad();

#endif
