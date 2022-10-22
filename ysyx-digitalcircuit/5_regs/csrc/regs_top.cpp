#include <nvboard.h>
#include "Vregs_top.h"

static TOP_NAME dut;

void nvboard_bind_all_pins(Vregs_top* regs_top);

static void single_cycle() {
    dut.i_clk = 0;
    dut.eval();
    dut.i_clk = 1;
    dut.eval();
}

int main() {
    nvboard_bind_all_pins(&dut);
    nvboard_init();

    while(1) {
        nvboard_update();
        single_cycle();
    }

    nvboard_quit();
}
