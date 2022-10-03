#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include <verilated.h>
#include <verilated_vcd_c.h>

#include "Vswitch.h"

int main(int argc, char** argv, char** env) {
    VerilatedContext* contextp = new VerilatedContext;
    contextp->commandArgs(argc, argv);
    Vswitch* switchp = new Vswitch{
        contextp
    };

    VerilatedVcdC* tfp = new VerilatedVcdC;
    contextp->traceEverOn(true);
    switchp->trace(tfp, 0);
    tfp->open("wave.vcd");

    while (!contextp->gotFinish()) {
        int a = rand() & 1;
        int b = rand() & 1;
        switchp->a = a;
        switchp->b = b;
        switchp->eval();
        printf("a = %d, b = %d, f = %d\n", a, b, switchp->f);

        tfp->dump(contextp->time());
        contextp->timeInc(1);

        assert(switchp->f == (a ^ b));
    }

    delete switchp;
    tfp->close();
    delete contextp;
    return 0;
}
