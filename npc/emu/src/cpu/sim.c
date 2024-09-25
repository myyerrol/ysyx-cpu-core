#include <sys/time.h>

#include <common.h>
#include <cpu/sim.h>
#include <debug/trace.h>
#include <device/keyboard.h>
#include <device/vga.h>
#include <memory/memory.h>
#include <state.h>
#include <utils/log.h>
#include <utils/timer.h>

typedef unsigned int       uint32_tt;
typedef unsigned long long uint64_tt;

bool sim_ebreak = false;

extern "C" void judgeIsEbreak(uint8_t inst_end_flag) {
    sim_ebreak = inst_end_flag;
}

extern "C" uint32_tt readInsData(uint64_tt addr, uint8_t len) {
    uint32_tt data = 0;
    if (addr != 0x00000000) {
        data = (uint32_tt)readPhyMemData(addr, len);
#ifdef CONFIG_MTRACE_COND_PROCESS
        printfDebugMTrace((char *)"process", (char *)"rd ins", addr, data, 0);
#endif
    }
    return data;
}

extern "C" uint64_tt readMemData(uint64_tt addr, uint8_t len) {
    uint64_tt data = 0;
    if (addr == 0xa0000048) {
        data = (uint64_tt)getTimerValue();
    }
    else if (addr == 0xa0000060) {
        data = (uint64_tt)dequeueDiviceKey();
    }
    else if (addr == 0xa0000100) {
        data = (uint64_tt)(getDeviceVGAScreenWidth() << 16 |
                           getDeviceVGAScreenHeight());
    }
    else if (judgeAddrIsInPhyMem(addr)) {
        data = (uint64_tt)readPhyMemData(addr, len);
    }
    else {
        data = 0;
    }
#ifdef CONFIG_MTRACE_COND_PROCESS
    printfDebugMTrace((char *)"process", (char *)"rd mem", addr, data, 0);
#endif
    return data;
}

extern "C" void writeMemData(uint64_tt addr, uint64_tt data, uint8_t len) {
    if (addr == 0xa00003f8) {
        putc((uint8_t)data, stderr);
    }
    else {
        writePhyMemData(addr, len, data);
    }
#ifdef CONFIG_MTRACE_COND_PROCESS
    printfDebugMTrace((char *)"process", (char *)"wr mem", addr, data, 0);
#endif
}

static bool inst_func_call = false;
static bool inst_func_ret  = false;

VerilatedContext *contextp = NULL;
#ifdef CONFIG_DEBUG_WAVE
VerilatedVcdC    *tfp = NULL;
#endif
VTop             *top = NULL;

static void runCPUSimStep() {
    top->eval();
#ifdef CONFIG_DEBUG_WAVE
    contextp->timeInc(1);
    tfp->dump(contextp->time());
#endif
}

static void runCPUSimModuleCycle() {
    top->clock = 0;
    runCPUSimStep();
    top->clock = 1;
    runCPUSimStep();
}

void initCPUSim() {
    contextp = new VerilatedContext;
#ifdef CONFIG_DEBUG_WAVE
    tfp = new VerilatedVcdC;
#endif
    top = new VTop;
    contextp->traceEverOn(true);
#ifdef CONFIG_DEBUG_WAVE
    top->trace(tfp, 0);
    tfp->open("build/cpu/wave.vcd");
#endif

#ifdef CONFIG_ITRACE_COND_PROCESS
    // top->io_iItrace = true;
#endif

#ifdef CONFIG_ETRACE_COND_PROCESS
    // top->io_iEtrace = true;
#endif
}

void exitCPUSim() {
    runCPUSimStep();
#ifdef CONFIG_DEBUG_WAVE
    tfp->close();
#endif
    delete top;
}

uint64_t sim_pc   = 0;
uint64_t sim_snpc = 0;
uint64_t sim_dnpc = 0;
uint64_t sim_inst = 0;
uint64_t sim_cycle_num = 1;

void runCPUSimModule(bool *inst_end_flag) {
    if (!sim_ebreak) {
        sim_pc   = top->io_pIFU_oPC;
        sim_snpc = sim_pc + 4;
        sim_inst = top->io_pIFU_oInst;

#ifdef CONFIG_ITRACE_COND_PROCESS
    LOG_BRIEF("[itrace] cycle num: %ld", sim_cycle_num);

    char *inst_name = (char *)"";
    switch (top->io_pITrace_pCTR_oInstName) {
        case  0: inst_name = (char *)"INST_NAME_X     "; break;
        case  1: inst_name = (char *)"INST_NAME_SLL   "; break;
        case  2: inst_name = (char *)"INST_NAME_SLLI  "; break;
        case  3: inst_name = (char *)"INST_NAME_SRL   "; break;
        case  4: inst_name = (char *)"INST_NAME_SRLI  "; break;
        case  5: inst_name = (char *)"INST_NAME_SRA   "; break;
        case  6: inst_name = (char *)"INST_NAME_SRAI  "; break;
        case  7: inst_name = (char *)"INST_NAME_ADD   "; break;
        case  8: inst_name = (char *)"INST_NAME_ADDI  "; break;
        case  9: inst_name = (char *)"INST_NAME_SUB   "; break;
        case 10: inst_name = (char *)"INST_NAME_LUI   "; break;
        case 11: inst_name = (char *)"INST_NAME_AUIPC "; break;
        case 12: inst_name = (char *)"INST_NAME_XOR   "; break;
        case 13: inst_name = (char *)"INST_NAME_XORI  "; break;
        case 14: inst_name = (char *)"INST_NAME_OR    "; break;
        case 15: inst_name = (char *)"INST_NAME_ORI   "; break;
        case 16: inst_name = (char *)"INST_NAME_AND   "; break;
        case 17: inst_name = (char *)"INST_NAME_ANDI  "; break;
        case 18: inst_name = (char *)"INST_NAME_SLT   "; break;
        case 19: inst_name = (char *)"INST_NAME_SLTI  "; break;
        case 20: inst_name = (char *)"INST_NAME_SLTU  "; break;
        case 21: inst_name = (char *)"INST_NAME_SLTIU "; break;
        case 22: inst_name = (char *)"INST_NAME_BEQ   "; break;
        case 23: inst_name = (char *)"INST_NAME_BNE   "; break;
        case 24: inst_name = (char *)"INST_NAME_BLT   "; break;
        case 25: inst_name = (char *)"INST_NAME_BGE   "; break;
        case 26: inst_name = (char *)"INST_NAME_BLTU  "; break;
        case 27: inst_name = (char *)"INST_NAME_BGEU  "; break;
        case 28: inst_name = (char *)"INST_NAME_JAL   "; break;
        case 29: inst_name = (char *)"INST_NAME_JALR  "; break;
        case 30: inst_name = (char *)"INST_NAME_FENCE "; break;
        case 31: inst_name = (char *)"INST_NAME_FENCEI"; break;
        case 32: inst_name = (char *)"INST_NAME_ECALL "; break;
        case 33: inst_name = (char *)"INST_NAME_EBREAK"; break;
        case 34: inst_name = (char *)"INST_NAME_CSRRW "; break;
        case 35: inst_name = (char *)"INST_NAME_CSRRS "; break;
        case 36: inst_name = (char *)"INST_NAME_CSRRC "; break;
        case 37: inst_name = (char *)"INST_NAME_CSRRWI"; break;
        case 38: inst_name = (char *)"INST_NAME_CSRRSI"; break;
        case 39: inst_name = (char *)"INST_NAME_CSRRCI"; break;
        case 40: inst_name = (char *)"INST_NAME_LB    "; break;
        case 41: inst_name = (char *)"INST_NAME_LH    "; break;
        case 42: inst_name = (char *)"INST_NAME_LBU   "; break;
        case 43: inst_name = (char *)"INST_NAME_LHU   "; break;
        case 44: inst_name = (char *)"INST_NAME_LW    "; break;
        case 45: inst_name = (char *)"INST_NAME_SB    "; break;
        case 46: inst_name = (char *)"INST_NAME_SH    "; break;
        case 47: inst_name = (char *)"INST_NAME_SW    "; break;
        case 48: inst_name = (char *)"INST_NAME_MUL   "; break;
        case 49: inst_name = (char *)"INST_NAME_MULH  "; break;
        case 50: inst_name = (char *)"INST_NAME_MULHSU"; break;
        case 51: inst_name = (char *)"INST_NAME_MULHU "; break;
        case 52: inst_name = (char *)"INST_NAME_DIV   "; break;
        case 53: inst_name = (char *)"INST_NAME_DIVU  "; break;
        case 54: inst_name = (char *)"INST_NAME_REM   "; break;
        case 55: inst_name = (char *)"INST_NAME_REMU  "; break;
        case 56: inst_name = (char *)"INST_NAME_MRET  "; break;
        case 57: inst_name = (char *)"INST_NAME_SLLW  "; break;
        case 58: inst_name = (char *)"INST_NAME_SLLIW "; break;
        case 59: inst_name = (char *)"INST_NAME_SRLW  "; break;
        case 60: inst_name = (char *)"INST_NAME_SRLIW "; break;
        case 61: inst_name = (char *)"INST_NAME_SRAW  "; break;
        case 62: inst_name = (char *)"INST_NAME_SRAIW "; break;
        case 63: inst_name = (char *)"INST_NAME_ADDW  "; break;
        case 64: inst_name = (char *)"INST_NAME_ADDIW "; break;
        case 65: inst_name = (char *)"INST_NAME_SUBW  "; break;
        case 66: inst_name = (char *)"INST_NAME_LWU   "; break;
        case 67: inst_name = (char *)"INST_NAME_LD    "; break;
        case 68: inst_name = (char *)"INST_NAME_SD    "; break;
        case 69: inst_name = (char *)"INST_NAME_MULW  "; break;
        case 70: inst_name = (char *)"INST_NAME_DIVW  "; break;
        case 71: inst_name = (char *)"INST_NAME_DIVUW "; break;
        case 72: inst_name = (char *)"INST_NAME_REMW  "; break;
        case 73: inst_name = (char *)"INST_NAME_REMUW "; break;
        default: inst_name = (char *)"X"               ; break;
    }
    LOG_BRIEF("[itrace] %s ", inst_name);

    LOG_BRIEF("[itrace]             cycle num:        %ld", sim_cycle_num);
    LOG_BRIEF("[itrace] [ifu]       pc:               " FMT_WORD,
              top->io_pIFU_oPC);
    LOG_BRIEF("[itrace] [ifu]       inst:             " FMT_WORD,
              (uint64_t)top->io_pIFU_oInst);

    char *state_curr = (char *)"";
    switch (top->io_pITrace_pCTR_oStateCurr) {
        case 0:  state_curr = (char *)"X";  break;
        case 1:  state_curr = (char *)"IF"; break;
        case 2:  state_curr = (char *)"ID"; break;
        case 3:  state_curr = (char *)"EX"; break;
        case 4:  state_curr = (char *)"LS"; break;
        case 5:  state_curr = (char *)"WB"; break;
        case 6:  state_curr = (char *)"ME"; break;
        default: state_curr = (char *)"X"; break;
    }
    LOG_BRIEF("[itrace] [idu] [ctr] state curr:       %s", state_curr);

    LOG_BRIEF("[itrace] [idu] [ctr] pc wr en:         %d",
              top->io_pITrace_pCTR_oPCWrEn);

    char *pc_wr_src = (char *)"";
    switch (top->io_pITrace_pCTR_oPCWrSrc) {
        case 0:  pc_wr_src = (char *)"X";    break;
        case 1:  pc_wr_src = (char *)"NEXT"; break;
        case 2:  pc_wr_src = (char *)"JUMP"; break;
        default: pc_wr_src = (char *)"X";    break;
    }
    LOG_BRIEF("[itrace] [idu] [ctr] pc wr src:        %s", pc_wr_src);

    LOG_BRIEF("[itrace] [idu] [ctr] pc next en:       %d",
              top->io_pITrace_pCTR_oPCNextEn);
    LOG_BRIEF("[itrace] [idu] [ctr] pc jump en:       %d",
              top->io_pITrace_pCTR_oPCJumpEn);
    LOG_BRIEF("[itrace] [idu] [ctr] mem rd en:        %d",
              top->io_pITrace_pCTR_oMemRdEn);

    char *mem_rd_src = (char *)"";
    switch (top->io_pITrace_pCTR_oMemRdSrc) {
        case 0:  mem_rd_src = (char *)"X";   break;
        case 1:  mem_rd_src = (char *)"PC";  break;
        case 2:  mem_rd_src = (char *)"ALU"; break;
        default: mem_rd_src = (char *)"X";   break;
    }
    LOG_BRIEF("[itrace] [idu] [ctr] mem rd src:       %s", mem_rd_src);

    LOG_BRIEF("[itrace] [idu] [ctr] mem wr en:        %d",
              top->io_pITrace_pCTR_oMemWrEn);

    char *mem_byt = (char *)"";
    switch (top->io_pITrace_pCTR_oMemByt) {
        case 0:  mem_byt = (char *)"X";   break;
        case 1:  mem_byt = (char *)"1_U"; break;
        case 2:  mem_byt = (char *)"2_U"; break;
        case 3:  mem_byt = (char *)"4_U"; break;
        case 4:  mem_byt = (char *)"8_U"; break;
        case 5:  mem_byt = (char *)"1_S"; break;
        case 6:  mem_byt = (char *)"2_S"; break;
        case 7:  mem_byt = (char *)"4_S"; break;
        case 8:  mem_byt = (char *)"8_S"; break;
        default: mem_byt = (char *)"X";   break;
    }
    LOG_BRIEF("[itrace] [idu] [ctr] mem byt:          %s", mem_byt);

    LOG_BRIEF("[itrace] [idu] [ctr] ir wr en:         %d",
              top->io_pITrace_pCTR_oIRWrEn);
    LOG_BRIEF("[itrace] [idu] [ctr] gpr wr en:        %d",
              top->io_pITrace_pCTR_oGPRWrEn);

    char *gpr_wr_src = (char *)"";
    switch (top->io_pITrace_pCTR_oGPRWrSrc) {
        case 0:  gpr_wr_src = (char *)"X";   break;
        case 1:  gpr_wr_src = (char *)"ALU"; break;
        case 2:  gpr_wr_src = (char *)"MEM"; break;
        default: gpr_wr_src = (char *)"X";   break;
    }
    LOG_BRIEF("[itrace] [idu] [ctr] gpr wr src:       %s", gpr_wr_src);

    char *alu_type = (char *)"";
    switch (top->io_pITrace_pCTR_oALUType) {
        case:  0: alu_type = (char *)"ALU_TYPE_X     "; break;
        case:  1: alu_type = (char *)"ALU_TYPE_SLL   "; break;
        case:  2: alu_type = (char *)"ALU_TYPE_SRL   "; break;
        case:  3: alu_type = (char *)"ALU_TYPE_SRA   "; break;
        case:  4: alu_type = (char *)"ALU_TYPE_ADD   "; break;
        case:  5: alu_type = (char *)"ALU_TYPE_SUB   "; break;
        case:  6: alu_type = (char *)"ALU_TYPE_XOR   "; break;
        case:  7: alu_type = (char *)"ALU_TYPE_OR    "; break;
        case:  8: alu_type = (char *)"ALU_TYPE_AND   "; break;
        case:  9: alu_type = (char *)"ALU_TYPE_SLT   "; break;
        case: 10: alu_type = (char *)"ALU_TYPE_SLTU  "; break;
        case: 11: alu_type = (char *)"ALU_TYPE_BEQ   "; break;
        case: 12: alu_type = (char *)"ALU_TYPE_BNE   "; break;
        case: 13: alu_type = (char *)"ALU_TYPE_BLT   "; break;
        case: 14: alu_type = (char *)"ALU_TYPE_BGE   "; break;
        case: 15: alu_type = (char *)"ALU_TYPE_BLTU  "; break;
        case: 16: alu_type = (char *)"ALU_TYPE_BGEU  "; break;
        case: 17: alu_type = (char *)"ALU_TYPE_JALR  "; break;
        case: 18: alu_type = (char *)"ALU_TYPE_MUL   "; break;
        case: 19: alu_type = (char *)"ALU_TYPE_MULH  "; break;
        case: 20: alu_type = (char *)"ALU_TYPE_MULHSU"; break;
        case: 21: alu_type = (char *)"ALU_TYPE_MULHU "; break;
        case: 22: alu_type = (char *)"ALU_TYPE_DIV   "; break;
        case: 23: alu_type = (char *)"ALU_TYPE_DIVU  "; break;
        case: 24: alu_type = (char *)"ALU_TYPE_REM   "; break;
        case: 25: alu_type = (char *)"ALU_TYPE_REMU  "; break;
        case: 26: alu_type = (char *)"ALU_TYPE_SLLW  "; break;
        case: 27: alu_type = (char *)"ALU_TYPE_SRLW  "; break;
        case: 28: alu_type = (char *)"ALU_TYPE_SRLIW "; break;
        case: 29: alu_type = (char *)"ALU_TYPE_SRAW  "; break;
        case: 30: alu_type = (char *)"ALU_TYPE_SRAIW "; break;
        case: 31: alu_type = (char *)"ALU_TYPE_DIVW  "; break;
        case: 32: alu_type = (char *)"ALU_TYPE_DIVUW "; break;
        case: 33: alu_type = (char *)"ALU_TYPE_REMW  "; break;
        case: 34: alu_type = (char *)"ALU_TYPE_REMUW "; break;
        default:  alu_type = (char *)"X";               break;
    }
    LOG_BRIEF("[itrace] [idu] [ctr] alu type:         %s", alu_type);

    char *alu_rs1 = (char *)"";
    switch (top->io_pITrace_pCTR_oALURS1) {
        case 0:  alu_rs1 = (char *)"X";   break;
        case 1:  alu_rs1 = (char *)"PC "; break;
        case 2:  alu_rs1 = (char *)"GPR"; break;
        default: alu_rs1 = (char *)"X";   break;
    }
    LOG_BRIEF("[itrace] [idu] [ctr] alu rs1:          %s", alu_rs1);

    char *alu_rs2 = (char *)"";
    switch (top->io_pITrace_pCTR_oALURS2) {
        case 0:  alu_rs2 = (char *)"X";     break;
        case 1:  alu_rs2 = (char *)"GPR";   break;
        case 2:  alu_rs2 = (char *)"IMM_I"; break;
        case 3:  alu_rs2 = (char *)"IMM_S"; break;
        case 4:  alu_rs2 = (char *)"IMM_B"; break;
        case 5:  alu_rs2 = (char *)"IMM_U"; break;
        case 6:  alu_rs2 = (char *)"IMM_J"; break;
        case 7:  alu_rs2 = (char *)"4";     break;
        default: alu_rs2 = (char *)"X";     break;
    }
    LOG_BRIEF("[itrace] [idu] [ctr] alu rs2:          %s", alu_rs2);

    LOG_BRIEF("[itrace] [idu]       rs1 addr:         %ld",
              top->io_pITrace_pIDU_oRS1Addr);
    LOG_BRIEF("[itrace] [idu]       rs2 addr:         %ld",
              top->io_pITrace_pIDU_oRS2Addr);
    LOG_BRIEF("[itrace] [idu]       rd  addr:         %ld",
              top->io_pITrace_pIDU_oRDAddr);
    LOG_BRIEF("[itrace] [idu]       rs1 data:         " FMT_WORD "",
              top->io_pITrace_pIDU_oRS1Data);
    LOG_BRIEF("[itrace] [idu]       rs2 data:         " FMT_WORD "",
              top->io_pITrace_pIDU_oRS2Data);
    LOG_BRIEF("[itrace] [idu]       end data:         " FMT_WORD "",
              top->io_pITrace_pIDU_oEndData);
    LOG_BRIEF("[itrace] [idu]       imm data:         " FMT_WORD "",
              top->io_pITrace_pIDU_oImmData);

    LOG_BRIEF("[itrace] [exu]       pc next:          " FMT_WORD "",
              top->io_pITrace_pEXU_oPCNext);
    LOG_BRIEF("[itrace] [exu]       pc jump:          " FMT_WORD "",
              top->io_pITrace_pEXU_oPCJump);
    LOG_BRIEF("[itrace] [exu]       alu zero:         %d",
              top->io_pITrace_pEXU_oALUZero);
    LOG_BRIEF("[itrace] [exu]       alu out:          " FMT_WORD "",
              top->io_pITrace_pEXU_oALUOut);

    // LOG_BRIEF("[itrace] [lsu]       mem rd en:        %d",
    //           top->io_pITrace_pLSU_oMemRdEn);
    // LOG_BRIEF("[itrace] [lsu]       mem rd addr inst: " FMT_WORD "",
    //           top->io_pITrace_pLSU_oMemRdAddrInst);
    // LOG_BRIEF("[itrace] [lsu]       mem rd addr load: " FMT_WORD "",
    //           top->io_pITrace_pLSU_oMemRdAddrLoad);
    // LOG_BRIEF("[itrace] [lsu]       mem rd data inst: " FMT_WORD "",
    //           top->io_pITrace_pLSU_oMemRdDataInst);
    // LOG_BRIEF("[itrace] [lsu]       mem rd data load: " FMT_WORD "",
    //           top->io_pITrace_pLSU_oMemRdDataLoad);
    // LOG_BRIEF("[itrace] [lsu]       mem wr en:        %d",
    //           top->io_pITrace_pLSU_oMemWrEn);
    // LOG_BRIEF("[itrace] [lsu]       mem wr addr:      " FMT_WORD "",
    //           top->io_pITrace_pLSU_oMemWrAddr);
    // LOG_BRIEF("[itrace] [lsu]       mem wr data:      " FMT_WORD "",
    //           top->io_pITrace_pLSU_oMemWrData);
    // LOG_BRIEF("[itrace] [lsu]       mem wr len:       %d",
    //           top->io_pITrace_pLSU_oMemWrLen);
    // LOG_BRIEF("[itrace] [lsu]       mem rd data:      " FMT_WORD "",
    //           top->io_pITrace_pLSU_oMemRdData);

    // LOG_BRIEF("[itrace] [wbu]       gpr wr:           " FMT_WORD "",
    //           top->io_pITrace_bWBUIO_oGPRWrData);
    LOG_BRIEF();
#endif

        runCPUSimModuleCycle();

        sim_dnpc = top->io_pIFU_oPC;
        sim_cycle_num++;

#if   CFLAGS_CPU_TYPE_SINGLE
        *inst_end_flag = true;
#elif CFLAGS_CPU_TYPE_MULTIP
        if (top->io_pITrace_pCTR_oStateCurr == 2) {
            *inst_end_flag = true;
        }
        else {
            *inst_end_flag = false;
        }
#elif CFLAGS_CPU_TYPE_PIPELINE

#endif


#ifdef CONFIG_FTRACE
        bool inst_func_call = top->io_oInstCall;
        bool inst_func_ret  = top->io_oInstRet;
#ifdef CONFIG_FTRACE_COND_PROCESS
        printfDebugFTrace((char *)"process",
                          inst_func_call,
                          inst_func_ret,
                          sim_pc,
                          sim_dnpc);
#else
        printfDebugFTrace((char *)"",
                          inst_func_call,
                          inst_func_ret,
                          sim_pc,
                          sim_dnpc);
#endif
#endif
    }

    if (sim_ebreak) {
        sim_cycle_num--;
        setNPCState(NPC_END, sim_pc, top->io_oEndData);
    }
}

void resetCPUSimModule(int num) {
    top->reset = 1;
    while (num-- > 0) {
        runCPUSimModuleCycle();
    }
    top->reset = 0;
}
