#include <getopt.h>
#include <time.h>

#include <common.h>
#include <debug/trace/trace.h>
#include <memory/host.h>
#include <memory/memory.h>
#include <monitor/monitor.h>
#include <monitor/sdb/sdb.h>
#include <utils/disasm.h>

static char *log_file = NULL;
static char *img_file = NULL;
static char *elf_file = NULL;
static char *diff_so_file  = NULL;
static int   difftest_port = 1234;

static long initMonitorImg() {
    if (img_file == NULL) {
        LOG("No image is given. Use the default build-in image.");
        return 4096;
    }

    FILE *fp = fopen(img_file, "rb");
    ASSERT(fp, "Can not open '%s'", img_file);

    fseek(fp, 0, SEEK_END);
    long size = ftell(fp);

    LOG("The image is %s, size = %ld", img_file, size);

    fseek(fp, 0, SEEK_SET);
    int ret = fread(convertGuestToHost(RESET_VECTOR), size, 1, fp);
    assert(ret == 1);

    fclose(fp);
    return size;
}

static void initMonitorISA() {
    static const uint32_t img[] = {
        0x00100093, // addi r1 r0 1
        0x00A00193, // addi r3 r0 10
        0x00100073  // ebreak
    };
    memcpy(convertGuestToHost(RESET_VECTOR), img, sizeof(img));
}

static void initMonitorRand() {
    srand(time(0));
}

static int parseMonitorArgs(int argc, char *argv[]) {
    const struct option table[] = {
        {"batch"    , no_argument      , NULL, 'b'},
        {"log"      , required_argument, NULL, 'l'},
        {"diff"     , required_argument, NULL, 'd'},
        {"port"     , required_argument, NULL, 'p'},
        {"help"     , no_argument      , NULL, 'h'},
        {"elf"      , required_argument, NULL, 'e'},
        {0          , 0                , NULL,  0 },
    };
    int opt;
    while ((opt = getopt_long(argc, argv, "-bhl:d:p:e:", table, NULL)) != -1) {
        switch (opt) {
            case 'b': setSDBBatchMode(); break;
            case 'p': sscanf(optarg, "%d", &difftest_port); break;
            case 'l': log_file = optarg; break;
            case 'd': diff_so_file = optarg; break;
            case 'e': elf_file = optarg; break;
            case   1: img_file = optarg; return 0;
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

static void printfWelcome() {
    LOG("Trace: %s",
        MUXDEF(CONFIG_TRACE, ANSI_FMT("ON", ANSI_FG_GREEN),
                             ANSI_FMT("OFF", ANSI_FG_RED)));
    IFDEF(CONFIG_TRACE,
          LOG("If trace is enabled, a log file will be generated "
              "to record the trace. This may lead to a large log file. "
              "If it is not necessary, you can disable it in menuconfig"));
    LOG("Build time: %s, %s", __TIME__, __DATE__);
    printf("Welcome to %s-NPC!\n", ANSI_FMT(str(__GUEST_ISA__),
                                   ANSI_FG_YELLOW ANSI_BG_RED));
    printf("For help, type \"h\"\n");
}

void initMonitor(int argc, char *argv[]) {
    parseMonitorArgs(argc, argv);

    initMonitorRand();

    initLog(log_file);
    initMem();

    initMonitorISA();
    initMonitorImg();

    initSDB();
    initDebugTrace(elf_file);

    IFDEF(CONFIG_ITRACE, initDisasm(
        MUXDEF(CONFIG_ISA_x86,     "i686",
        MUXDEF(CONFIG_ISA_mips32,  "mipsel",
        MUXDEF(CONFIG_ISA_riscv32, "riscv32",
        MUXDEF(CONFIG_ISA_riscv64, "riscv64", "bad")))) "-pc-linux-gnu"
    ));

    printfWelcome();

    loopSDB();
}
