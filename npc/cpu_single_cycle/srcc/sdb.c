#include <readline/readline.h>
#include <readline/history.h>

#include "sdb.h"

#include "cpu.h"
#include "reg.h"
#include "sim.h"


static char* rl_gets() {
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
    cpu_exec(-1);
    return 0;
}

static int cmd_q(char *args) {
    set_npc_state(NPC_QUIT, 0, 0);
    return -1;
}

static int cmd_s(char *args) {
    char *args_n = strtok(args, " ");
    uint64_t n = 1;
    if (args_n != NULL) {
        n = strtoul(args_n, NULL, 10);
    }
    cpu_exec(n);
    return 0;
}

static int cmd_i(char *args) {
    char *args_t = strtok(args, " ");
    if (args_t != NULL) {
        if (strcmp(args_t, "r") == 0) {
            isa_reg_display();
        }
        else if (strcmp(args_t, "w") == 0) {
            // watch_display();
        }
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
    { "q", "Exit NEMU.", cmd_q },
    { "s", "Step one instruction exactly.", cmd_s },
    { "i", "Generic command for showing things about the program being debugged.", cmd_i },
    // { "x", "Address.", cmd_x },
    // { "p", "Print value of expression EXP.", cmd_p },
    // { "w", "Set a watchpoint for an expression.", cmd_w },
    // { "d", "Delete all or some breakpoints.", cmd_d }
};

#define NR_CMD ARRLEN(cmd_table)

static int cmd_h(char *args) {
    char *arg = strtok(NULL, " ");
    int i;

    if (arg == NULL) {
        for (i = 0; i < NR_CMD; i ++) {
            printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
        }
    }
    else {
        for (i = 0; i < NR_CMD; i ++) {
            if (strcmp(arg, cmd_table[i].name) == 0) {
                printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
                return 0;
            }
        }
        printf("Unknown command '%s'\n", arg);
    }
    return 0;
}





static int is_batch_mode = false;

void sdb_set_batch_mode() {
    is_batch_mode = true;
}

void sdb_mainloop() {
    initSim();
    resetSimModule(1);

    if (is_batch_mode) {
        cmd_c(NULL);
        return;
    }

    for (char *str; (str = rl_gets()) != NULL; ) {
        char *str_end = str + strlen(str);

        char *cmd = strtok(str, " ");
        if (cmd == NULL) { continue; }

        char *args = cmd + strlen(cmd) + 1;
        if (args >= str_end) {
            args = NULL;
        }

        int i;
        for (i = 0; i < NR_CMD; i ++) {
            if (strcmp(cmd, cmd_table[i].name) == 0) {
                if (cmd_table[i].handler(args) < 0) { return; }
                break;
            }
        }

        if (i == NR_CMD) { printf("Unknown command '%s'\n", cmd); }
    }

    exitSim();
    return;
}
