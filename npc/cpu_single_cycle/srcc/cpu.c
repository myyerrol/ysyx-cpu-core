#include <locale.h>

#include <common.h>
#include <isa.h>

#include "sim.h"
#include "state.h"

// #define MAX_INST_TO_PRINT 10

CPU_state cpu = {};
uint64_t g_nr_guest_inst = 0;
static uint64_t g_timer = 0;
// static bool g_print_step = true;

void set_npc_state(int state, vaddr_t pc, int halt_ret) {
    npc_state.state = state;
    npc_state.halt_pc = pc;
    npc_state.halt_ret = halt_ret;
}

static void exec_once() {
    runSimModule();
}

static void execute(uint64_t n) {
    for (; n > 0; n--) {
        exec_once();
        g_nr_guest_inst++;
        if (npc_state.state != NPC_RUNNING) {
            break;
        }
    }
}

static void statistic() {
    IFNDEF(CONFIG_TARGET_AM, setlocale(LC_NUMERIC, ""));
    #define NUMBERIC_FMT MUXDEF(CONFIG_TARGET_AM, "%", "%'") PRIu64
    Log("host time spent = " NUMBERIC_FMT " us", g_timer);
    Log("total guest instructions = " NUMBERIC_FMT, g_nr_guest_inst);
    if (g_timer > 0) Log("simulation frequency = " NUMBERIC_FMT " inst/s", g_nr_guest_inst * 1000000 / g_timer);
    else Log("Finish running in less than 1 us and can not calculate the simulation frequency");
}

void assert_fail_msg() {
}

void cpu_exec(uint64_t n) {
    switch (npc_state.state) {
        case NPC_END: case NPC_ABORT:
            printf("Program execution has ended. To restart the program, exit NPC and run again.\n");
            return;
        default: npc_state.state = NPC_RUNNING;
    }

    uint64_t timer_start = get_time();

    execute(n);

    uint64_t timer_end = get_time();
    g_timer += timer_end - timer_start;

    switch (npc_state.state) {
        case NPC_RUNNING: npc_state.state = NPC_STOP; break;

        case NPC_END: case NPC_ABORT:
        Log("npc: %s at pc = " FMT_WORD,
            (npc_state.state == NPC_ABORT ? ANSI_FMT("ABORT", ANSI_FG_RED) :
            (npc_state.halt_ret == 0 ? ANSI_FMT("HIT GOOD TRAP", ANSI_FG_GREEN) :
                ANSI_FMT("HIT BAD TRAP", ANSI_FG_RED))),
            npc_state.halt_pc);
        case NPC_QUIT: statistic();
    }
}
