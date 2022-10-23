
#include <nvboard.h>
#include "Vchar_top.h"

static TOP_NAME dut;

void nvboard_bind_all_pins(Vchar_top* char_top);

static void single_cycle() {
    dut.i_clk = 0;
    dut.eval();
    dut.i_clk = 1;
    dut.eval();
}

static void reset(int n) {
    dut.i_rst = 1;
    while (n-- > 0) {
        single_cycle();
    }
    dut.i_rst = 0;
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
