#ifndef __SIM_H__
#define __SIM_H__

#include <verilated.h>
#include <verilated_vcd_c.h>
#include <VTop.h>
#include <VTop__Dpi.h>

extern VerilatedContext *contextp;
extern VerilatedVcdC    *tfp;
extern VTop             *top;

void runSimStep();
void exitSim();
void initSim();
void runSimModuleCycle();
void runSimModule();
void resetSimModule(int n);

#endif
