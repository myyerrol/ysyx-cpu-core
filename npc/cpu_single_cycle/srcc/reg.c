#include "reg.h"
#include "sim.h"

const char *regs[] = {
  "$0", "ra", "sp",  "gp",  "tp", "t0", "t1", "t2",
  "s0", "s1", "a0",  "a1",  "a2", "a3", "a4", "a5",
  "a6", "a7", "s2",  "s3",  "s4", "s5", "s6", "s7",
  "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
};

uint64_t gpr(int i) {
    switch (i) {
        case 0:  return top->io_oRegData0;
        case 1:  return top->io_oRegData1;
        case 2:  return top->io_oRegData2;
        case 3:  return top->io_oRegData3;
        case 4:  return top->io_oRegData4;
        case 5:  return top->io_oRegData5;
        case 6:  return top->io_oRegData6;
        case 7:  return top->io_oRegData7;
        case 8:  return top->io_oRegData8;
        case 9:  return top->io_oRegData9;
        case 10: return top->io_oRegData10;
        case 11: return top->io_oRegData11;
        case 12: return top->io_oRegData12;
        case 13: return top->io_oRegData13;
        case 14: return top->io_oRegData14;
        case 15: return top->io_oRegData15;
        case 16: return top->io_oRegData16;
        case 17: return top->io_oRegData17;
        case 18: return top->io_oRegData18;
        case 19: return top->io_oRegData19;
        case 20: return top->io_oRegData20;
        case 21: return top->io_oRegData21;
        case 22: return top->io_oRegData22;
        case 23: return top->io_oRegData23;
        case 24: return top->io_oRegData24;
        case 25: return top->io_oRegData25;
        case 26: return top->io_oRegData26;
        case 27: return top->io_oRegData27;
        case 28: return top->io_oRegData28;
        case 29: return top->io_oRegData29;
        case 30: return top->io_oRegData30;
        case 31: return top->io_oRegData31;
        default: return top->io_oRegData0;
    }
}

void isa_reg_display() {
  char *space_num = "";
  char *space_reg = "";
  char *exist_str = "";
  for (int i = 0; i < ARRLEN(regs); i++) {
    space_num = (i < 10) ? (char *)" " : (char *)"";
    space_reg = (strcmp(regs[i], "s10") != 0 &&
                 strcmp(regs[i], "s11") != 0) ? (char *)" " : (char *)"";
    exist_str = (gpr(i) != 0) ? (char *)ANSI_FMT("*", ANSI_FG_GREEN) : (char *)"";
    printf("[sdb reg] i: %d%s val: %s%s = " FMT_WORD "%s\n",
           i,
           space_num,
           space_reg,
           regs[i],
           gpr(i),
           exist_str);
  }
}
