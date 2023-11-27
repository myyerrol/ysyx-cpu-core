#include <getopt.h>
#include <time.h>

#include <common.h>
#include <debug/difftest.h>
#include <debug/trace.h>
#include <device/device.h>
#include <isa/isa.h>
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
        LOG_BRIEF_COLOR("[monitor] image: use the default build-in");
        return 4096;
    }

    FILE *fp = fopen(img_file, "rb");
    ASSERT(fp, "[monitor] can not open '%s'", img_file);

    fseek(fp, 0, SEEK_END);
    long size = ftell(fp);

    LOG_BRIEF_COLOR("[monitor] image: %s, size = %ld byte", img_file, size);

    fseek(fp, 0, SEEK_SET);
    int ret = fread(convertGuestToHost(RESET_VECTOR), size, 1, fp);
    assert(ret == 1);

    fclose(fp);
    return size;
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

static void printfMonitorWelcome() {
    LOG_BRIEF_COLOR("[monitor] [welcome] [sdb]:               %s",
                    MUXDEF(CONFIG_SDB, ANSI_FMT("ON",  ANSI_FG_GREEN),
                                       ANSI_FMT("OFF", ANSI_FG_RED)));
    LOG_BRIEF_COLOR("[monitor] [welcome] [trace]:             %s",
                    MUXDEF(CONFIG_TRACE, ANSI_FMT("ON",  ANSI_FG_GREEN),
                                         ANSI_FMT("OFF", ANSI_FG_RED)));
    LOG_BRIEF_COLOR("[monitor] [welcome] [trace] inst:        %s",
                    MUXDEF(CONFIG_ITRACE, ANSI_FMT("ON",  ANSI_FG_GREEN),
                                          ANSI_FMT("OFF", ANSI_FG_RED)));
    LOG_BRIEF_COLOR("[monitor] [welcome] [trace]     process: %s",
                    MUXDEF(CONFIG_ITRACE_COND_PROCESS,
                           ANSI_FMT("ON",  ANSI_FG_GREEN),
                           ANSI_FMT("OFF", ANSI_FG_RED)));
    LOG_BRIEF_COLOR("[monitor] [welcome] [trace]     result:  %s",
                    MUXDEF(CONFIG_ITRACE_COND_RESULT,
                           ANSI_FMT("ON",  ANSI_FG_GREEN),
                           ANSI_FMT("OFF", ANSI_FG_RED)));

    LOG_BRIEF_COLOR("[monitor] [welcome] [trace] memory:      %s",
                    MUXDEF(CONFIG_MTRACE, ANSI_FMT("ON",  ANSI_FG_GREEN),
                                          ANSI_FMT("OFF", ANSI_FG_RED)));
    LOG_BRIEF_COLOR("[monitor] [welcome] [trace]     process: %s",
                    MUXDEF(CONFIG_MTRACE_COND_PROCESS,
                           ANSI_FMT("ON",  ANSI_FG_GREEN),
                           ANSI_FMT("OFF", ANSI_FG_RED)));
    LOG_BRIEF_COLOR("[monitor] [welcome] [trace]     result:  %s",
                    MUXDEF(CONFIG_MTRACE_COND_RESULT,
                           ANSI_FMT("ON",  ANSI_FG_GREEN),
                           ANSI_FMT("OFF", ANSI_FG_RED)));

    LOG_BRIEF_COLOR("[monitor] [welcome] [trace] function:    %s",
                    MUXDEF(CONFIG_FTRACE, ANSI_FMT("ON",  ANSI_FG_GREEN),
                                          ANSI_FMT("OFF", ANSI_FG_RED)));
    LOG_BRIEF_COLOR("[monitor] [welcome] [trace]     process: %s",
                    MUXDEF(CONFIG_FTRACE_COND_PROCESS,
                           ANSI_FMT("ON",  ANSI_FG_GREEN),
                           ANSI_FMT("OFF", ANSI_FG_RED)));
    LOG_BRIEF_COLOR("[monitor] [welcome] [trace]     result:  %s",
                    MUXDEF(CONFIG_FTRACE_COND_RESULT,
                           ANSI_FMT("ON",  ANSI_FG_GREEN),
                           ANSI_FMT("OFF", ANSI_FG_RED)));

    LOG_BRIEF_COLOR("[monitor] [welcome] [trace] device:      %s",
                    MUXDEF(CONFIG_DTRACE, ANSI_FMT("ON",  ANSI_FG_GREEN),
                                          ANSI_FMT("OFF", ANSI_FG_RED)));
    LOG_BRIEF_COLOR("[monitor] [welcome] [trace]     process: %s",
                    MUXDEF(CONFIG_DTRACE_COND_PROCESS,
                          ANSI_FMT("ON",  ANSI_FG_GREEN),
                          ANSI_FMT("OFF", ANSI_FG_RED)));
    LOG_BRIEF_COLOR("[monitor] [welcome] [trace]     result:  %s",
                    MUXDEF(CONFIG_DTRACE_COND_RESULT,
                           ANSI_FMT("ON",  ANSI_FG_GREEN),
                           ANSI_FMT("OFF", ANSI_FG_RED)));

    LOG_BRIEF_COLOR("[monitor] [welcome] [trace] exception:   %s",
                    MUXDEF(CONFIG_ETRACE, ANSI_FMT("ON",  ANSI_FG_GREEN),
                                          ANSI_FMT("OFF", ANSI_FG_RED)));
    LOG_BRIEF_COLOR("[monitor] [welcome] [trace]     process: %s",
                    MUXDEF(CONFIG_ETRACE_COND_PROCESS,
                           ANSI_FMT("ON",  ANSI_FG_GREEN),
                           ANSI_FMT("OFF", ANSI_FG_RED)));
    LOG_BRIEF_COLOR("[monitor] [welcome] [trace]     result:  %s",
                    MUXDEF(CONFIG_ETRACE_COND_RESULT,
                           ANSI_FMT("ON",  ANSI_FG_GREEN),
                           ANSI_FMT("OFF", ANSI_FG_RED)));

    LOG_BRIEF_COLOR("[monitor] [welcome] [difftest]:          %s",
                    MUXDEF(CONFIG_DIFFTEST, ANSI_FMT("ON",  ANSI_FG_GREEN),
                                            ANSI_FMT("OFF", ANSI_FG_RED)));
    LOG_BRIEF_COLOR("[monitor] [welcome] [difftest] ref:      %s",
                   CONFIG_DIFFTEST_REF_NAME);

    LOG_BRIEF_COLOR("[monitor] [welcome] [wave]:              %s",
                    MUXDEF(CONFIG_DEBUG_WAVE, ANSI_FMT("ON",  ANSI_FG_GREEN),
                                              ANSI_FMT("OFF", ANSI_FG_RED)));

    LOG_BRIEF_COLOR("[monitor] [welcome] date: %s, %s", __TIME__, __DATE__);

    LOG_BRIEF("Welcome to %s-NPC!", ANSI_FMT(str(__GUEST_ISA__),
                                             ANSI_FG_YELLOW ANSI_BG_RED));
    LOG_BRIEF("For help, type \"h\"");
}

void initMonitor(int argc, char *argv[]) {
    parseMonitorArgs(argc, argv);

    initMonitorRand();

    initLog(log_file);
    initMem();
    initISA();

    long img_size = initMonitorImg();
    genMemFile("/home/myyerrol/Workspaces/mem.txt", img_size);

    initDebugDifftest(diff_so_file, img_size, difftest_port);

    initSDB();
    initDebugTrace(elf_file);

    IFDEF(CONFIG_ITRACE, initDisasm(
        MUXDEF(CONFIG_ISA_x86,     "i686",
        MUXDEF(CONFIG_ISA_mips32,  "mipsel",
        MUXDEF(CONFIG_ISA_riscv32, "riscv32",
        MUXDEF(CONFIG_ISA_riscv64, "riscv64", "bad")))) "-pc-linux-gnu"
    ));
    IFDEF(CONFIG_DEVICE, initDevice());

    printfMonitorWelcome();

    loopSDB();
}
