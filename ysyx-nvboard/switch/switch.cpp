#include <nvboard.h>
#include "Vswitch.h"

static TOP_NAME dut;

void nvboard_bind_all_pins(Vswitch* switchp);

int main() {
    nvboard_bind_all_pins(&dut);
    nvboard_init();

    while (1) {
        nvboard_update();
        dut.eval();
    }

    nvboard_quit();
}
