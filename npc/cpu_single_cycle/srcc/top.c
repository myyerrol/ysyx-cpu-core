#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include <common.h>
#include <memory/paddr.h>

#include <verilated.h>
#include <verilated_vcd_c.h>
#include "VTop.h"
#include "VTop__Dpi.h"

// 存储器函数
// static uint8_t host_mem[CONFIG_MSIZE] PG_ALIGN = {};

// static uint8_t *convertGuestToHost(paddr_t paddr) {
//     return host_mem + paddr - CONFIG_MBASE;
// }

// static void initGuestMemory() {
//     static const uint32_t inst[] = {
//         0x00100093,
//         0x00A00193,
//         0x00100073
//     };
//     memcpy(convertGuestToHost(RESET_VECTOR), inst, sizeof(inst));
// }

// static word_t readHost(void *addr, int len) {
//     switch (len) {
//         case 1: return *(uint8_t  *)addr;
//         case 2: return *(uint16_t *)addr;
//         case 4: return *(uint32_t *)addr;
//         IFDEF(CONFIG_ISA64, case 8: return *(uint64_t *)addr);
//         default: MUXDEF(CONFIG_RT_CHECK, assert(0), return 0);
//     }
// }

// static word_t readHostMemory(paddr_t addr, int len) {
//     word_t ret = readHost(convertGuestToHost(addr), len);
//     return ret;
// }


void initMem() {
    static const uint32_t inst[] = {
        0x00100093,
        0x00A00193,
        0x00100073
    };
    memcpy(guest_to_host(RESET_VECTOR), inst, sizeof(inst));
}

// 仿真测试函数
VerilatedContext *contextp = NULL;
VerilatedVcdC *tfp = NULL;
static VTop *top;

static void runSimStep(){
    top->eval();
    contextp->timeInc(1);
    tfp->dump(contextp->time());
}

static void exitSim() {
    runSimStep();
    tfp->close();
    delete top;
}

static void initSim() {
    contextp = new VerilatedContext;
    tfp = new VerilatedVcdC;
    top = new VTop;
    contextp->traceEverOn(true);
    top->trace(tfp, 0);
    tfp->open("build/cpu_single_cycle/Wave.vcd");
}

static void runSimModuleCycle(VTop *top) {
    top->clock = 0;
    runSimStep();
    top->clock = 1;
    runSimStep();
}

static void resetSimModule(VTop *top, int n) {
    top->reset = 1;
    while (n-- > 0) {
        runSimModuleCycle(top);
    }
    top->reset = 0;
}

// DPI-C函数
int ebreak_flag = 0;

void judgeIsEbreak(int flag) {
    ebreak_flag = flag;
}

int main(int argc, char **argv, char **env) {
    if (false && argc && argv && env) {
    }

    // initGuestMemory();
    initMem();
    initSim();

    resetSimModule(top, 1);

    while (!ebreak_flag) {
        // top->io_iInst = readHostMemory(top->io_oPC, 8);
        top->io_iInst = paddr_read(top->io_oPC, 8);
        runSimModuleCycle(top);
    }

    exitSim();

    return 0;
}
