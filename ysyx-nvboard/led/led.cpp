#include <nvboard.h>
#include "Vled.h"

static TOP_NAME dut;

void nvboard_bind_all_pins(Vled* led);

static void simple_cycle() {
    dut.clk = 0;
    dut.eval();
    dut.clk = 1;
    dut.eval();
}

static void reset(int n) {
    dut.rst = 1;
    while (n-- > 0) {
        simple_cycle();
    }
    dut.rst = 0;
}

int main() {
    nvboard_bind_all_pins(&dut);
    nvboard_init();

    reset(10);

    while(1) {
        nvboard_update();
        simple_cycle();
    }

    nvboard_quit();
}
