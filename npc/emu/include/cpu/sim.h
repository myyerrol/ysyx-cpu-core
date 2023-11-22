#ifndef __SIM_H__
#define __SIM_H__

#include <verilated.h>
#include <verilated_vcd_c.h>
#include <VTop.h>
#include <VTop__Dpi.h>

extern VTop *top;

void exitCPUSim();
void initCPUSim();
void runCPUSimModule(bool *inst_end_flag);
void resetCPUSimModule(int num);

#endif
