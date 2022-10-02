#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include <verilated.h>

#include "Vtop.h"

int main(int argc, char** argv, char** env) {
    if (false && argc && argv && env) {
    }

    Vtop* top = new Vtop;

    while (!Verilated::gotFinish()) {
        int a = rand() & 1;
        int b = rand() & 1;
        top->a = a;
        top->b = b;
        top->eval();
        printf("a = %d, b = %d, f = %d\n", a, b, top->f);
        assert(top->f == (a ^ b));
    }

    delete top;
    return 0;
}
