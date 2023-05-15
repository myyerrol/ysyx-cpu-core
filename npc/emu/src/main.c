#include <common.h>

#include <monitor/monitor.h>
#include <state.h>

int main(int argc, char *argv[]) {
    initMonitor(argc, argv);

    return judgeNPCStateIsBad();
}