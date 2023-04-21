#include "sdb.h"
#include "cpu.h"

#include "sim.h"

static int cmd_c(char *args) {
    cpu_exec(-1);
    return 0;
}

void sdb_mainloop() {
    initSim();
    resetSimModule(1);

    cmd_c(NULL);

    exitSim();
    return;
}

