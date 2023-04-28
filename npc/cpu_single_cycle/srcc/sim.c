#include <common.h>
#include <memory/paddr.h>

#include "sim.h"
#include "cpu.h"

typedef unsigned long long uint64_tt;

int ebreak_flag = 0;

extern "C" void judgeIsEbreak(int flag) {
    ebreak_flag = flag;
}

extern "C" long long readMemData(uint64_tt addr, char len) {
    long long data = 0;
    if (likely(in_pmem(addr))) {
        data = paddr_read(addr, len);
    }
    // printf("c mem rd addr: " FMT_WORD "\n", (uint64_t)addr);
    // printf("c mem rd data: " FMT_WORD "\n", (uint64_t)data);
    return data;
}

extern "C" void writeMemData(uint64_tt addr, uint64_tt data, char len) {
    if (likely(in_pmem(addr))) {
        paddr_write(addr, len, data);
    }
    // printf("c mem wr addr: " FMT_WORD "\n", (uint64_t)addr);
    // printf("c mem wr data: " FMT_WORD "\n", (uint64_t)data);
}

static VerilatedContext *contextp = NULL;
static VerilatedVcdC    *tfp = NULL;
static VTop             *top = NULL;

void runSimStep() {
    top->eval();
    contextp->timeInc(1);
    tfp->dump(contextp->time());
}

void exitSim() {
    runSimStep();
    tfp->close();
    delete top;
}

void initSim() {
    // svSetScope(svGetScopeFromName("TOP.Top.dpi"));

    contextp = new VerilatedContext;
    tfp = new VerilatedVcdC;
    top = new VTop;
    contextp->traceEverOn(true);
    top->trace(tfp, 0);
    tfp->open("build/cpu_single_cycle/Wave.vcd");
}

void runSimModuleCycle() {
    top->clock = 0;
    runSimStep();
    top->clock = 1;
    runSimStep();
}

void resetSimModule(int n) {
    top->reset = 1;
    while (n-- > 0) {
        runSimModuleCycle();
    }
    top->reset = 0;
}

void runSimModule() {
    if (!ebreak_flag) {
        runSimModuleCycle();
    }
    else {
        // for (int i = 0; i < 200; i++) {
        //     word_t addr = RESET_VECTOR + (i * 4);
        //     printf("addr: " FMT_WORD " ", addr);
        //     word_t data = paddr_read(addr, 8);
        //     printf("data: " FMT_WORD "\n", data);
        // }
        NPC_INST_TRAP(top->io_oPC - 4, top->io_oReg);
    }
}
