#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include <common.h>
#include <memory/paddr.h>

#include <verilated.h>
#include "VTop.h"
#include "VTop__Dpi.h"

static uint8_t pmem[CONFIG_MSIZE] PG_ALIGN = {};

uint8_t* guest_to_host(paddr_t paddr) { return pmem + paddr - CONFIG_MBASE; }

static inline word_t host_read(void *addr, int len) {
    switch (len) {
        case 1: return *(uint8_t  *)addr;
        case 2: return *(uint16_t *)addr;
        case 4: return *(uint32_t *)addr;
        IFDEF(CONFIG_ISA64, case 8: return *(uint64_t *)addr);
        default: MUXDEF(CONFIG_RT_CHECK, assert(0), return 0);
    }
}

static inline void host_write(void *addr, int len, word_t data) {
    switch (len) {
        case 1: *(uint8_t  *)addr = data; return;
        case 2: *(uint16_t *)addr = data; return;
        case 4: *(uint32_t *)addr = data; return;
        IFDEF(CONFIG_ISA64, case 8: *(uint64_t *)addr = data; return);
        IFDEF(CONFIG_RT_CHECK, default: assert(0));
    }
}

static word_t pmem_read(paddr_t addr, int len) {
  word_t ret = host_read(guest_to_host(addr), len);
  return ret;
}

static void pmem_write(paddr_t addr, int len, word_t data) {
  host_write(guest_to_host(addr), len, data);
}

static void single_cycle(VTop* top) {
    top->clock = 0;
    top->eval();
    top->clock = 1;
    top->eval();
}

static void reset(VTop* top, int n) {
    top->reset = 1;
    while (n-- > 0) {
        single_cycle(top);
    }
    top->reset = 0;
}

int add(int a, int b) { return a+b; }

int judgeEbreak(int flag) {

}

int main(int argc, char** argv, char** env) {
    if (false && argc && argv && env) {
    }

    static const uint32_t inst[] = {
        0x00100093,
        0x00A00193,
        0x00100073
    };
    memcpy(guest_to_host(RESET_VECTOR), inst, sizeof(inst));

    VTop* top = new VTop;
    reset(top, 10);

    // while (!Verilated::gotFinish()) {
    while (!top->io_oHalt) {
        top->io_iInst = pmem_read(top->io_oPC, 8);
        single_cycle(top);
    }

    delete top;
    return 0;
}
