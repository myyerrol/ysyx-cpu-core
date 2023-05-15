#include <dlfcn.h>

#include <isa/difftest.h>
#include <isa/isa.h>
#include <debug/difftest.h>
#include <memory/memory.h>
#include <state.h>

typedef void (*handleDifftestMemcpyRefT)(paddr_t addr, void *buf, size_t n, bool direction);
handleDifftestMemcpyRefT handleDifftestMemcpyRef = NULL;

typedef void (*handleDifftestRegcpyT)(void *dut, bool direction);
handleDifftestRegcpyT handleDifftestRegcpy = NULL;

typedef void (*handleDifftestExecT)(uint64_t num);
handleDifftestExecT handleDifftestExec = NULL;

typedef void (*handleDifftestRaiseIntrT)(uint64_t no);
handleDifftestRaiseIntrT handleDifftestRaiseIntr = NULL;

typedef void (*handleDifftestInitT)(int);
handleDifftestInitT handleDifftestInit = NULL;

#ifdef CONFIG_DIFFTEST

static bool difftest_skip_ref = false;
static int difftest_skip_dut_num = 0;

static void checkDebugDifftestRegs(CPUState *cpu_ref, vaddr_t pc) {
    if (!checkISADifftestRegs(cpu_ref, pc)) {
        npc_state.state = NPC_ABORT;
        npc_state.halt_pc = pc;
        printfISADifftest(cpu_ref, pc);
    }
}

void initDebugDifftest(char *ref_so_file, long img_size, int port) {
    assert(ref_so_file != NULL);

    void *handle;
    handle = dlopen(ref_so_file, RTLD_LAZY);
    assert(handle);

    handleDifftestMemcpyRef = (handleDifftestMemcpyRefT)dlsym(
        handle,
        "difftest_memcpy");
    assert(handleDifftestMemcpyRef);

    handleDifftestRegcpy = (handleDifftestRegcpyT)dlsym(
        handle,
        "difftest_regcpy");
    assert(handleDifftestRegcpy);

    handleDifftestExec = (handleDifftestExecT)dlsym(
        handle,
        "difftest_exec");
    assert(handleDifftestExec);

    handleDifftestRaiseIntr = (handleDifftestRaiseIntrT)dlsym(
        handle,
        "difftest_raise_intr");
    assert(handleDifftestRaiseIntr);

    handleDifftestInit = (handleDifftestInitT)dlsym(handle, "difftest_init");
    assert(handleDifftestInit);

    LOG("Differential testing: %s", ANSI_FMT("ON", ANSI_FG_GREEN));
    LOG("The result of every instruction will be compared with %s. "
        "This will help you a lot for debugging, but also significantly reduce the performance. "
        "If it is not necessary, you can turn it off in menuconfig.", ref_so_file);

    handleDifftestInit(port);
    handleDifftestMemcpyRef(RESET_VECTOR,
                            convertGuestToHost(RESET_VECTOR),
                            img_size,
                            DIFFTEST_TO_REF);
    handleDifftestRegcpy(&cpu, DIFFTEST_TO_REF);
}

void skipDebugDifftestRef() {
    difftest_skip_ref = true;
    difftest_skip_dut_num = 0;
}

void skipDebugDifftestDut(int ref_num, int dut_num) {
    difftest_skip_dut_num += dut_num;

    while (ref_num-- > 0) {
        handleDifftestExec(1);
    }
}

void stepDebugDifftest(vaddr_t pc, vaddr_t npc) {
    CPUState cpu_ref;

    if (difftest_skip_dut_num > 0) {
        handleDifftestRegcpy(&cpu_ref, DIFFTEST_TO_DUT);
        if (cpu_ref.pc == npc) {
            difftest_skip_dut_num = 0;
            checkDebugDifftestRegs(&cpu_ref, npc);
            return;
        }
        difftest_skip_dut_num--;
        if (difftest_skip_dut_num == 0) {
            PANIC("[difftest] can not catch up with ref.pc = " \
                  FMT_WORD " at dut.pc = " FMT_WORD, cpu_ref.pc, pc);
        }
        return;
    }

    if (difftest_skip_ref) {
        handleDifftestRegcpy(&cpu, DIFFTEST_TO_REF);
        difftest_skip_ref = false;
        return;
    }

    handleDifftestExec(1);
    handleDifftestRegcpy(&cpu_ref, DIFFTEST_TO_DUT);

    checkDebugDifftestRegs(&cpu_ref, pc);
}
#else
void initDebugDifftest(char *ref_so_file, long img_size, int port) { }
#endif
