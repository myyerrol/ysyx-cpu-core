#include <nvboard.h>
#include "Vmux_top.h"

static TOP_NAME dut;

void nvboard_bind_all_pins(Vmux_top* mux_top);

int main() {
    nvboard_bind_all_pins(&dut);
    nvboard_init();

    while (1) {
        nvboard_update();
        dut.eval();
    }

    nvboard_quit();
}
