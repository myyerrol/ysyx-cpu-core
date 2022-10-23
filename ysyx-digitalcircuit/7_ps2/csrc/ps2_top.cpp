#include <nvboard.h>
#include "Vps2_top.h"

static TOP_NAME dut;

void nvboard_bind_all_pins(Vps2_top* fsm_top);

static void single_cycle() {
    dut.i_clk = 0;
    dut.eval();
    dut.i_clk = 1;
    dut.eval();
}

static void reset(int n) {
    dut.i_clr_n = 0;
    while (n-- > 0) {
    }
    dut.i_clr_n = 1;
}

int main() {
    nvboard_bind_all_pins(&dut);
    nvboard_init();

    reset(10);

    while(1) {
        nvboard_update();
        single_cycle();
    }

    nvboard_quit();
}
