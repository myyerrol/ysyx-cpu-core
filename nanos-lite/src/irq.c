#include <common.h>

extern void do_syscall(Context *c);

static Context* do_event(Event e, Context* c) {
  switch (e.event) {
    case EVENT_YIELD: printf("[irq] event hit: yeild\n"); c->mepc = c->mepc + 4; break;
    case EVENT_SYSCALL: do_syscall(c); c->mepc = c->mepc + 4; break;
    case EVENT_ERROR:  panic("[irq] event hit: error"); break;
    default: panic("Unhandled event ID = %d", e.event);
  }

  return c;
}

void init_irq(void) {
  Log("Initializing interrupt/exception handler...");
  cte_init(do_event);
}
