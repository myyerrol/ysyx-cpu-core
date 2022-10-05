#include <nvboard.h>
#include "Vshifter_top.h"

static TOP_NAME dut;

void nvboard_bind_all_pins(Vshifter_top* shifter_top);

int main() {
    nvboard_bind_all_pins(&dut);
    nvboard_init();

    while(1) {
        nvboard_update();
        dut.eval();
    }

    nvboard_quit();
}
