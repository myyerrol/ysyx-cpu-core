#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include <verilated.h>

#include <memory/paddr.h>

#include "VTop.h"

int main(int argc, char** argv, char** env) {
    if (false && argc && argv && env) {
    }

    VTop* top = new VTop;

    while (!Verilated::gotFinish()) {
        top->io_iInst = pmem_read(top->io_oPC)
        top->eval();
        printf("pc = %ld, inst = %d, result = %ld, halt = %d\n",
               top->io_oPC,
               top->io_iInst,
               top->io_oInstRDVal,
               top->io_oHalt);
        // printf("a = %d, b = %d, f = %d\n", a, b, switchp->f);
        // assert(switchp->f == (a ^ b));
    }

    delete top;
    return 0;
}
