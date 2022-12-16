/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <isa.h>
#include <cpu/cpu.h>
#include <readline/readline.h>
#include <readline/history.h>
#include "sdb.h"

#include <memory/paddr.h>

#define DEBUG_CMD_ARGS 0

static int is_batch_mode = false;
static int cmd_p_index = 1;

void init_regex();
void init_wp_pool();

/* We use the `readline' library to provide more flexibility to read from stdin. */
static char* rl_gets() {
  static char *line_read = NULL;

  if (line_read) {
    free(line_read);
    line_read = NULL;
  }

  line_read = readline("(nemu) ");

  if (line_read && *line_read) {
    add_history(line_read);
  }

  return line_read;
}

static int cmd_c(char *args) {
  cpu_exec(-1);
  return 0;
}

static int cmd_q(char *args) {
  set_nemu_state(NEMU_QUIT, 0, 0);
  return -1;
}

static int cmd_help(char *args);

static int cmd_si(char *args) {
  char *args_n = strtok(args, " ");
  uint64_t n = 1;
  if (args_n != NULL) {
    n = atol(args_n);
  }
  cpu_exec(n);
  return 0;
}

static int cmd_info(char *args) {
  char *args_t = strtok(args, " ");
  if (args_t != NULL) {
    if (strcmp(args_t, "r") == 0) {
      isa_reg_display();
    }
    else if (strcmp(args_t, "w") == 0) {
      watch_display();
    }
  }
  return 0;
}

static int cmd_x(char *args) {
  char *args_n = strtok(args, " ");

  if (strcmp(args_n, "test") == 0) {
    expr_test();
  }
  else {
    char *args_expr = strtok(NULL, " ");
    if (args_n != NULL && args_expr != NULL) {
      bool success = false;
      uint32_t n = strtoul(args_n, NULL, 10);
      uint32_t addr = expr(args_expr, NULL, &success);
      for (uint32_t i = 0; i < n; i++) {
        printf("0x%016"PRIx32"     =     0x%016"PRIx64"\n",
               addr,
               paddr_read(addr, 8));
        addr = addr + 4;
      }
    }
  }
  return 0;
}

static int cmd_p(char *args) {
  char *args_expr = strtok(args, " ");
  if (args_expr != NULL) {
    bool success = false;
    word_t ret = expr(args_expr, NULL, &success);
    printf("$%d = %lu\n", cmd_p_index, ret);
    cmd_p_index++;
  }
  return 0;
}

static int cmd_w(char *args) {
  char *args_expr = strtok(args, " ");
  if (strcmp(args_expr, "test") == 0) {
    watch_test();
  }
  else {
    printf("Hardware watchpoint %d: %s\n", watch_new(args_expr), args_expr);
  }
  return 0;
}

static int cmd_d(char *args) {

  return 0;
}

static struct {
  const char *name;
  const char *description;
  int (*handler) (char *);
} cmd_table [] = {
  { "help", "Display information about all supported commands.", cmd_help },
  { "c", "Continue the execution of the program.", cmd_c },
  { "q", "Exit NEMU.", cmd_q },

  /* TODO: Add more commands */
  { "si", "Step one instruction exactly.", cmd_si },
  { "info", "Generic command for showing things about the program being debugged.", cmd_info },
  { "x", "Address.", cmd_x },
  { "p", "Print value of expression EXP.", cmd_p },
  { "w", "Set a watchpoint for an expression.", cmd_w },
  { "d", "Delete all or some breakpoints.", cmd_d }
};

#define NR_CMD ARRLEN(cmd_table)

static int cmd_help(char *args) {
  /* extract the first argument */
  char *arg = strtok(NULL, " ");
  int i;

  if (arg == NULL) {
    /* no argument given */
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

void sdb_set_batch_mode() {
  is_batch_mode = true;
}

void sdb_mainloop() {
  if (is_batch_mode) {
    cmd_c(NULL);
    return;
  }

  for (char *str; (str = rl_gets()) != NULL; ) {
    char *str_end = str + strlen(str);

    /* extract the first token as the command */
    char *cmd = strtok(str, " ");
    if (cmd == NULL) { continue; }

    /* treat the remaining string as the arguments,
     * which may need further parsing
     */
    char *args = cmd + strlen(cmd) + 1;
    if (args >= str_end) {
      args = NULL;
    }
#if DEBUG_CMD_ARGS
    Log("command args: %s", args);
#endif

#ifdef CONFIG_DEVICE
    extern void sdl_clear_event_queue();
    sdl_clear_event_queue();
#endif

    int i;
    for (i = 0; i < NR_CMD; i ++) {
      if (strcmp(cmd, cmd_table[i].name) == 0) {
        if (cmd_table[i].handler(args) < 0) { return; }
        break;
      }
    }

    if (i == NR_CMD) { printf("Unknown command '%s'\n", cmd); }
  }
}

void init_sdb() {
  /* Compile the regular expressions. */
  init_regex();

  /* Initialize the watchpoint pool. */
  init_wp_pool();
}
