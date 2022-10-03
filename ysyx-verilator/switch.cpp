#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include <verilated.h>

#include "Vswitch.h"

int main(int argc, char** argv, char** env) {
    if (false && argc && argv && env) {
    }

    Vswitch* switchp = new Vswitch;

    while (!Verilated::gotFinish()) {
        int a = rand() & 1;
        int b = rand() & 1;
        switchp->a = a;
        switchp->b = b;
        switchp->eval();
        printf("a = %d, b = %d, f = %d\n", a, b, switchp->f);
        assert(switchp->f == (a ^ b));
    }

    delete switchp;
    return 0;
}
