#include <isa/difftest.h>
#include <isa/isa.h>
#include <isa/reg.h>

bool checkISADifftestRegs(CPUState *cpu_ref, vaddr_t pc) {
    for (int i = 0; i < 32; i++) {
        if (cpu_ref->gpr[i] != getISAReg(i)) {
            printf("[difftest] error at pc: " FMT_WORD "\n", pc);
            return false;
        }
    }
    return true;
}

void printfISADifftest(CPUState *cpu_ref, vaddr_t pc) {
    char *space_num = (char *)"";
    char *space_reg = (char *)"";
    char *error_str = (char *)"";
    for (int i = 0; i < 32; i++) {
        const char *name = getISARegName(i);
        space_num = (i < 10) ? (char *)" " : (char *)"";
        space_reg = (strcmp(name, "s10") != 0 &&
                     strcmp(name, "s11") != 0) ? (char *)" " : (char *)"";
        error_str = (cpu_ref->gpr[i] != getISAReg(i)) ?
                        (char *)ANSI_FMT("*", ANSI_FG_RED) : (char *)"";
        printf("[difftest] reg i: %d%s dut val: %s%s = " FMT_WORD \
               " ref val: %s%s = " FMT_WORD "%s\n",
               i,
               space_num,
               space_reg,
               name,
               getISAReg(i),
               space_reg,
               name,
               cpu_ref->gpr[i],
               error_str);
    }
}
