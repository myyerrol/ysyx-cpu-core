#include <sys/time.h>

#include <common.h>
#include <cpu/sim.h>
#include <debug/trace.h>
#include <device/keyboard.h>
#include <device/vga.h>
#include <memory/memory.h>
#include <state.h>
#include <utils/log.h>
#include <utils/timer.h>

typedef unsigned long long uint64_tt;

bool sim_ebreak = false;

extern "C" void judgeIsEbreak(uint8_t flag) {
    sim_ebreak = flag;
}

extern "C" uint64_tt readInsData(uint64_tt addr, uint8_t len) {
    uint64_tt data = 0;
    if (addr != 0x00000000) {
        data = (uint64_tt)readPhyMemData(addr, len);
#ifdef CONFIG_MTRACE_COND_PROCESS
        printfDebugMTrace((char *)"process", (char *)"rd ins", addr, data, 0);
#endif
    }
    return data;
}

extern "C" uint64_tt readMemData(uint64_tt addr, uint8_t len) {
    uint64_tt data = 0;
    if (addr == 0xa0000048) {
        data = (uint64_tt)getTimerValue();
    }
    else if (addr == 0xa0000060) {
        data = (uint64_tt)dequeueDiviceKey();
    }
    else if (addr == 0xa0000100) {
        data = (uint64_tt)(getDeviceVGAScreenWidth() << 16 |
                           getDeviceVGAScreenHeight());
    }
    else if (judgeAddrIsInPhyMem(addr)) {
        data = (uint64_tt)readPhyMemData(addr, len);
    }
    else {
        return data;
    }
#ifdef CONFIG_MTRACE_COND_PROCESS
    printfDebugMTrace((char *)"process", (char *)"rd mem", addr, data, 0);
#endif
    return data;
}

extern "C" void writeMemData(uint64_tt addr, uint64_tt data, uint8_t len) {
    if (addr == 0xa00003f8) {
        putc((uint8_t)data, stderr);
        // IFDEF(CONFIG_HAS_SERIAL, putc((uint8_t)data, stderr));
    }
    // else if (addr == 0xa0000104) {

    // }
    // else if (addr == 0xb0000000) {

    // }
    else {
        writePhyMemData(addr, len, data);
    }
#ifdef CONFIG_MTRACE_COND_PROCESS
    printfDebugMTrace((char *)"process", (char *)"wr mem", addr, data, 0);
#endif
}

static bool inst_func_call = false;
static bool inst_func_ret  = false;

VerilatedContext *contextp = NULL;
VerilatedVcdC    *tfp = NULL;
VTop             *top = NULL;

static void runCPUSimStep() {
    top->eval();
    contextp->timeInc(1);
    tfp->dump(contextp->time());
}

static void runCPUSimModuleCycle() {
    top->clock = 0;
    runCPUSimStep();
    top->clock = 1;
    runCPUSimStep();
}

void initCPUSim() {
    contextp = new VerilatedContext;
    tfp = new VerilatedVcdC;
    top = new VTop;
    contextp->traceEverOn(true);
    top->trace(tfp, 0);
    tfp->open("build/cpu/Wave.vcd");

#ifdef CONFIG_ITRACE_COND_PROCESS
    top->io_iItrace = true;
#endif
}

void exitCPUSim() {
    runCPUSimStep();
    tfp->close();
    delete top;
}

uint64_t sim_pc   = 0;
uint64_t sim_snpc = 0;
uint64_t sim_dnpc = 0;
uint64_t sim_inst = 0;

void runCPUSimModule() {
    if (!sim_ebreak) {
        sim_pc = top->io_oPC;
        sim_snpc = sim_pc + 4;
        sim_inst = top->io_oInst;

        bool inst_func_call = top->io_oInstCall;
        bool inst_func_ret  = top->io_oInstRet;

        runCPUSimModuleCycle();

        sim_dnpc = top->io_oPC;

#ifdef CONFIG_FTRACE
#ifdef CONFIG_FTRACE_COND_PROCESS
        printfDebugFTrace((char *)"process",
                          inst_func_call,
                          inst_func_ret,
                          sim_pc,
                          sim_dnpc);
#else
        printfDebugFTrace((char *)"",
                          inst_func_call,
                          inst_func_ret,
                          sim_pc,
                          sim_dnpc);
#endif
#endif
    }

    if (sim_ebreak) {
        setNPCState(NPC_END, sim_pc, top->io_oRegRdEndData);
    }
}

void resetCPUSimModule(int num) {
    top->reset = 1;
    while (num-- > 0) {
        runCPUSimModuleCycle();
    }
    top->reset = 0;
}
