#include <nvboard.h>
#include "Vregs_top.h"

static TOP_NAME dut;

void nvboard_bind_all_pins(Vregs_top* regstop);

static void simple_cycle() {
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
        simple_cycle();
    }

    nvboard_quit();
}
