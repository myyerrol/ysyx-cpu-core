#include <common.h>
#include <memory/paddr.h>
#include "sim.h"
#include "cpu.h"

int ebreak_flag = 0;

extern "C" void judgeIsEbreak(int flag) {
    ebreak_flag = flag;
}

extern "C" long long readMemData(long long addr) {
    long long data = 0;
    long long addr_t = addr & ~0x7ull;
    if (likely(in_pmem(addr_t))) {
        data = paddr_read(addr_t, 8);
    }
    printf("c mem rd addr: " FMT_WORD "\n", addr);
    printf("c mem rd data: " FMT_WORD "\n", data);
    return data;
}

extern "C" void writeMemData(long long addr, long long data, char len) {
    printf("c mem wr addr: %llx\n", addr);
    printf("c mem wr data: %llx\n", data);
    long long addr_t = addr & ~0x7ull;
    if (likely(in_pmem(addr_t))) {
        paddr_write(addr_t & ~0x7ull, len, data);
    }
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
        top->io_iInst = paddr_read(top->io_oPC, 4);
        runSimModuleCycle();
    }
    else {
        NPC_INST_TRAP(top->io_oPC - 4, top->io_oReg);
    }
}
