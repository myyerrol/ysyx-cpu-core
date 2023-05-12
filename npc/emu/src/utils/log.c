#include <common.h>

extern uint64_t cpu_guest_inst;

FILE *log_fp = NULL;

void initLog(const char *log_file) {
    log_fp = stdout;
    if (log_file != NULL) {
        FILE *fp = fopen(log_file, "w");
        ASSERT(fp, "Can not open '%s'", log_file);
        log_fp = fp;
    }
    LOG("Log is written to %s", log_file ? log_file : "stdout");
}

bool enaLog() {
  return MUXDEF(CONFIG_TRACE, (cpu_guest_inst >= CONFIG_TRACE_START) &&
                              (cpu_guest_inst <= CONFIG_TRACE_END), false);
}
