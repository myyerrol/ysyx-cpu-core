#include <readline/readline.h>
#include <readline/history.h>

#include <cpu/cpu.h>
#include <cpu/sim.h>
#include <device/device.h>
#include <isa/gpr.h>
#include <memory/memory.h>
#include <monitor/sdb/expr.h>
#include <monitor/sdb/watch.h>
#include <monitor/sdb/sdb.h>
#include <state.h>

static int sdb_cmp_p_index = 1;
static int sdb_batch_mode  = false;

static char* getSDBCmdArgs() {
    static char *line_read = NULL;

    if (line_read) {
        free(line_read);
        line_read = NULL;
    }

    line_read = readline("(npc) ");

    if (line_read && *line_read) {
        add_history(line_read);
    }

    return line_read;
}

static int cmd_h(char *args);

static int cmd_c(char *args) {
    execCPU(-1);
    return 0;
}

static int cmd_q(char *args) {
    setNPCState(NPC_QUIT, 0, 0);
    return -1;
}

static int cmd_s(char *args) {
    char *args_n = strtok(args, " ");
    uint64_t n = 1;
    if (args_n != NULL) {
        n = strtoul(args_n, NULL, 10);
    }
    execCPU(n);
    return 0;
}

static int cmd_i(char *args) {
    bool flag = false;
    char *args_t = strtok(args, " ");
    if (args_t != NULL) {
        if (strcmp(args_t, "r") == 0) {
            printfISAGPRData();
        }
        else if (strcmp(args_t, "w") == 0) {
            printfSDBWatch();
        }
        else {
            flag = true;
        }
    }
    else {
        flag = false;
    }

    if (flag) {
        LOG_BRIEF("[sdb] [cmd] please use the format in the example: " \
                  "[i r] or [i w]");
    }
    return 0;
}

static int cmd_x(char *args) {
    bool flag = false;
    char *args_n = strtok(args, " ");
    if (args_n != NULL) {
        if (strcmp(args_n, "test") == 0) {
            testSDBExpr();
        }
        else {
            char *args_expr = strtok(NULL, " ");
            if (args_n != NULL && args_expr != NULL) {
                bool success = false;
                uint32_t n = strtoul(args_n, NULL, 10);
                uint32_t addr = handleSDBExpr(args_expr, NULL, &success);
                for (uint32_t i = 0; i < n; i++) {
                    LOG_BRIEF(
                        "[sdb] [cmd] addr: 0x%016" PRIx32" = 0x%016" PRIx64"",
                        addr,
                        readPhyMemData(addr, 4));
                    addr = addr + 4;
                }
            }
            else {
                flag = true;
            }
        }
    }
    else {
        flag = true;
    }

    if (flag) {
        LOG_BRIEF("[sdb] [cmd] please use the format in the example: " \
                 "[x test] or [x 10 0x80000000]");
    }
    return 0;
}

static int cmd_p(char *args) {
    char *args_expr = strtok(args, " ");
    if (args_expr != NULL) {
        bool success = false;
        word_t ret = handleSDBExpr(args_expr, NULL, &success);
        LOG_BRIEF("[sdb] [cmd] $%d = %lu", sdb_cmp_p_index, ret);
        sdb_cmp_p_index++;
    }
    else {
        LOG_BRIEF("[sdb] [cmd] please use the format in the example: [p 1+2*3]");
    }
    return 0;
}

static int cmd_w(char *args) {
    char *args_expr = strtok(args, " ");
    if (args_expr != NULL) {
        if (strcmp(args_expr, "test") == 0) {
            testSDBWatch();
        }
        else {
            newSDBWatch(args_expr);
        }
    }
    else {
        LOG_BRIEF("[sdb] [cmd] please use the format in the example: " \
                  "[w test] or [w 1+2*3]");
    }
    return 0;
}

static int cmd_d(char *args) {
    char *args_no = strtok(args, " ");
    if (args_no != NULL) {
        int no = strtol(args_no, NULL, 10);
        freeSDBWatch(no);
    }
    else {
        LOG_BRIEF("[sdb] [cmd] please use the format in the example: [d 1]");
    }
    return 0;
}

static struct {
    const char *name;
    const char *description;
    int (*handler) (char *);
} cmd_table [] = {
    { "h", "Display information about all supported commands.", cmd_h },
    { "c", "Continue the execution of the program.", cmd_c },
    { "q", "Exit NPC.", cmd_q },
    { "s", "Step one instruction exactly.", cmd_s },
    { "i", "Generic command for showing things about the program being debugged.", cmd_i },
    { "x", "Address.", cmd_x },
    { "p", "Print value of expression EXP.", cmd_p },
    { "w", "Set a watchpoint for an expression.", cmd_w },
    { "d", "Delete all or some breakpoints.", cmd_d }
};

#define NR_CMD ARRLEN(cmd_table)

static int cmd_h(char *args) {
    char *arg = strtok(NULL, " ");
    int i;

    if (arg == NULL) {
        for (i = 0; i < NR_CMD; i ++) {
            LOG_BRIEF("[sdb] [cmd] %s: %s", cmd_table[i].name,
                                            cmd_table[i].description);
        }
    }
    else {
        for (i = 0; i < NR_CMD; i ++) {
            if (strcmp(arg, cmd_table[i].name) == 0) {
                LOG_BRIEF("[sdb] [cmd] %s: %s", cmd_table[i].name,
                                                cmd_table[i].description);
                return 0;
            }
        }
        LOG_BRIEF("[sdb] [cmd] Unknown command: %s", arg);
    }
    return 0;
}

void initSDB() {
    initSDBExpr();
    initSDBWatch();
}

void loopSDB() {
    initCPUSim();
    resetCPUSimModule(1);

    if (sdb_batch_mode) {
        cmd_c(NULL);
        return;
    }

    for (char *str; (str = getSDBCmdArgs()) != NULL; ) {
        char *str_end = str + strlen(str);

        char *cmd = strtok(str, " ");
        if (cmd == NULL) { continue; }

        char *args = cmd + strlen(cmd) + 1;
        if (args >= str_end) {
            args = NULL;
        }
#ifdef CONFIG_SDB_CMD
        LOG_BRIEF("[sdb] [cmd] command: %s, args: %s", cmd, args);
#endif

#ifdef CONFIG_DEVICE
    clearDeviceEventQueue();
#endif

        int i;
        for (i = 0; i < NR_CMD; i ++) {
            if (strcmp(cmd, cmd_table[i].name) == 0) {
                if (cmd_table[i].handler(args) < 0) { return; }
                break;
            }
        }

        if (i == NR_CMD) { LOG_BRIEF("[sdb] [cmd] Unknown command: %s", cmd); }
    }

    exitCPUSim();
    return;
}

void setSDBBatchMode() {
    sdb_batch_mode = true;
}
