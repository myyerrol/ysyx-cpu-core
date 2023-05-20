#ifndef __VGA_H__
#define __VGA_H__

#include <common.h>

uint32_t getDeviceVGAScreenWidth();
uint32_t getDeviceVGAScreenHeight();

void initDeviceVGA();
void updateDeviceVGAScreen();

#endif
