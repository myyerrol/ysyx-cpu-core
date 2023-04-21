#include <memory/paddr.h>
#include "sim.h"
#include "cpu.h"

int ebreak_flag = 0;

void judgeIsEbreak(int flag) {
    ebreak_flag = flag;
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
        NPC_INST_TRAP(top->io_oPC, top->io_oReg);
    }
}
