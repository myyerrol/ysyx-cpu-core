#include <getopt.h>

#include <common.h>
#include <memory/paddr.h>

#include <verilated.h>
#include <verilated_vcd_c.h>
#include "VTop.h"
#include "VTop__Dpi.h"

static char *log_file = NULL;
static char *img_file = NULL;

// 兼容执行函数
void init_log(const char *log_file);

// 初始执行函数
static int parseArgs(int argc, char **argv) {
    const struct option table[] = {
        {"batch"    , no_argument      , NULL, 'b'},
        {"log"      , required_argument, NULL, 'l'},
        {"diff"     , required_argument, NULL, 'd'},
        {"port"     , required_argument, NULL, 'p'},
        {"help"     , no_argument      , NULL, 'h'},
        {"elf"      , required_argument, NULL, 'e'},
        {0          , 0                , NULL,  0 },
    };
    int o;
    while ((o = getopt_long(argc, argv, "-bhl:d:p:e:", table, NULL)) != -1) {
        switch (o) {
        // case 'b': sdb_set_batch_mode(); break;
        // case 'p': sscanf(optarg, "%d", &difftest_port); break;
        case 'l': log_file = optarg; break;
        // case 'd': diff_so_file = optarg; break;
        // case 'e': elf_file = optarg; break;
        case 1: img_file = optarg; return 0;
        default:
            printf("Usage: %s [OPTION...] IMAGE [args]\n\n", argv[0]);
            printf("\t-b,--batch              run with batch mode\n");
            printf("\t-l,--log=FILE           output log to FILE\n");
            printf("\t-d,--diff=REF_SO        run DiffTest with reference REF_SO\n");
            printf("\t-p,--port=PORT          run DiffTest with port PORT\n");
            printf("\t-e,--elf=FILE           read symbol and string table from FILE\n");
            printf("\n");
            exit(0);
        }
    }
    return 0;
}

static void initLog() {
    init_log(log_file);
}

static void initISA() {
    static const uint32_t img[] = {
        0x00100093, // addi r1 r0 1
        0x00A00193, // addi r3 r0 10
        0x00100073  // ebreak
    };
    memcpy(guest_to_host(RESET_VECTOR), img, sizeof(img));
}

static long initImg() {
    if (img_file == NULL) {
        Log("No image is given. Use the default build-in image.");
        return 4096;
    }

    FILE *fp = fopen(img_file, "rb");
    Assert(fp, "Can not open '%s'", img_file);

    fseek(fp, 0, SEEK_END);
    long size = ftell(fp);

    Log("The image is %s, size = %ld", img_file, size);

    fseek(fp, 0, SEEK_SET);
    int ret = fread(guest_to_host(RESET_VECTOR), size, 1, fp);
    assert(ret == 1);

    fclose(fp);
    return size;
}

static void initMonitor(int argc, char **argv) {
    parseArgs(argc, argv);

    initLog();
    initISA();
    initImg();
}

// 仿真测试函数
VerilatedContext *contextp = NULL;
VerilatedVcdC *tfp = NULL;
static VTop *top;

static void runSimStep(){
    top->eval();
    contextp->timeInc(1);
    tfp->dump(contextp->time());
}

static void exitSim() {
    runSimStep();
    tfp->close();
    delete top;
}

static void initSim() {
    contextp = new VerilatedContext;
    tfp = new VerilatedVcdC;
    top = new VTop;
    contextp->traceEverOn(true);
    top->trace(tfp, 0);
    tfp->open("build/cpu_single_cycle/Wave.vcd");
}

static void runSimModuleCycle(VTop *top) {
    top->clock = 0;
    runSimStep();
    top->clock = 1;
    runSimStep();
}

static void resetSimModule(VTop *top, int n) {
    top->reset = 1;
    while (n-- > 0) {
        runSimModuleCycle(top);
    }
    top->reset = 0;
}

// 硬件通信函数
int ebreak_flag = 0;

void judgeIsEbreak(int flag) {
    ebreak_flag = flag;
}

// 主要执行函数
int main(int argc, char **argv, char **env) {
    if (false && argc && argv && env) {
    }

    initMonitor(argc, argv);
    initSim();

    resetSimModule(top, 1);

    while (!ebreak_flag) {
        top->io_iInst = paddr_read(top->io_oPC, 8);
        runSimModuleCycle(top);
    }

    exitSim();

    return 0;
}
