#include <locale.h>

#include <cpu/cpu.h>
#include <cpu/sim.h>
#include <debug/trace/trace.h>
#include <isa/reg.h>
#include <state.h>
#include <monitor/sdb/watch.h>
#include <utils/timer.h>

#define MAX_INST_TO_PRINT 10

static uint64_t cpu_timer = 0;
static bool cpu_print_step = true;

uint64_t cpu_guest_inst = -1;

static void execCPUTraceAndDifftest() {
#ifdef CONFIG_ITRACE_COND
    writeLog("%s\n", _this->logbuf);
#endif

#ifdef CONFIG_ITRACE_COND_RESULT
    printfDebugITrace(_this->logbuf);
#endif

#ifdef CONFIG_SDB_WATCH
    if (traceSDBWatch() > 0) {
        npc_state.state = NPC_STOP;
    }
#endif

//   if (cpu_print_step) { IFDEF(CONFIG_ITRACE, puts(_this->logbuf)); }
//   IFDEF(CONFIG_DIFFTEST, difftest_step(_this->pc, dnpc));
    // difftest_step(_this->pc, dnpc);
}

static void execCPUTimesSingle() {
    runCPUSimModule();

#ifdef CONFIG_ITRACE_CMD_RESULT
    // char *p = s->logbuf;
    // p += snprintf(p, sizeof(s->logbuf), FMT_WORD, s->pc);
    // int ilen = s->snpc - s->pc;
    // int i;
    // uint8_t *inst = (uint8_t *)&s->isa.inst.val;
    // for (i = ilen - 1; i >= 0; i --) {
    //     p += snprintf(p, 4, " %02x", inst[i]);
    // }
    // int ilen_max = MUXDEF(CONFIG_ISA_x86, 8, 4);
    // int space_len = ilen_max - ilen;
    // if (space_len < 0) space_len = 0;
    // space_len = space_len * 3 + 1;
    // memset(p, ' ', space_len);
    // p += space_len;

    // void disassemble(char *str, int size, uint64_t pc, uint8_t *code, int nbyte);
    // disassemble(p, s->logbuf + sizeof(s->logbuf) - p,
    //     MUXDEF(CONFIG_ISA_x86, s->snpc, s->pc), (uint8_t *)&s->isa.inst.val, ilen);
#endif
}

static void execCPUTimesMultip(uint64_t num) {
    for (; num > 0; num--) {
        execCPUTimesSingle();
        cpu_guest_inst++;
        // execCPUTraceAndDifftest(&s, cpu.pc);
        if (npc_state.state != NPC_RUNNING) {
            break;
        }
    }
}

static void printfCPUStat() {
    IFNDEF(CONFIG_TARGET_AM, setlocale(LC_NUMERIC, ""));
    #define NUMBERIC_FMT MUXDEF(CONFIG_TARGET_AM, "%", "%'") PRIu64
    LOG("host time spent = " NUMBERIC_FMT " us", cpu_timer);
    LOG("total guest instructions = " NUMBERIC_FMT, cpu_guest_inst);
    if (cpu_timer > 0) {
        LOG("simulation frequency = " NUMBERIC_FMT " inst/s",
            cpu_guest_inst * 1000000 / cpu_timer);
    }
    else {
        LOG("Finish running in less than 1 us and can not calculate the " \
            "simulation frequency");
    }
}

void execCPU(uint64_t num) {
    cpu_print_step = (num < MAX_INST_TO_PRINT);
    switch (npc_state.state) {
        case NPC_END: case NPC_ABORT:
            printf("Program execution has ended. To restart the program, " \
                   "exit NPC and run again.\n");
            return;
        default: npc_state.state = NPC_RUNNING;
    }

    uint64_t timer_start = getTimerValue();

    execCPUTimesMultip(num);

    uint64_t timer_end = getTimerValue();
    cpu_timer += timer_end - timer_start;

    switch (npc_state.state) {
        case NPC_RUNNING: { npc_state.state = NPC_STOP; break; }
        case NPC_END: case NPC_ABORT: {
#ifdef CONFIG_ITRACE_COND_RESULT
#ifndef CONFIG_ITRACE_COND_PROCESS
            printf("\n");
#endif
            printfDebugITrace();
#endif
#ifdef CONFIG_MTRACE_COND_RESULT
            printf("\n");
            printfDebugMTrace((char *)"result", NULL, 0, 0, 16);
#endif
#ifdef CONFIG_FTRACE_COND_RESULT
            printf("\n");
            printfDebugFTrace((char *)"result", NULL, NULL, 0, 0);
#endif
            LOG("npc: %s at pc = " FMT_WORD,
                (npc_state.state == NPC_ABORT ?
                 ANSI_FMT("ABORT", ANSI_FG_RED) :
                (npc_state.halt_ret == 0 ?
                 ANSI_FMT("HIT GOOD TRAP", ANSI_FG_GREEN) :
                 ANSI_FMT("HIT BAD TRAP", ANSI_FG_RED))),
                 npc_state.halt_pc);
        }
        case NPC_QUIT: { printfCPUStat(); }
    }
}

void printfCPUAssertFailMsg() {
    printfISARegData();
    printfCPUStat();
}
