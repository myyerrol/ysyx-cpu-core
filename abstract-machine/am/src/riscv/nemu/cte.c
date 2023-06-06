#include <am.h>
#include <riscv/riscv.h>
#include <klib.h>

static Context* (*user_handler)(Event, Context*) = NULL;

Context* __am_irq_handle(Context *c) {
  if (user_handler) {
    Event ev = {0};
    int mcause = c->mcause;
    mcause = (mcause >= 0 && mcause <= 19) ? 1 : mcause;
    switch (mcause) {
      case  1: ev.event = EVENT_SYSCALL; break;
      case -1: ev.event = EVENT_YIELD;   break;
      default: ev.event = EVENT_ERROR;   break;
    }

    c = user_handler(ev, c);
    assert(c != NULL);

    switch (c->GPRx) {
      case 0: halt(0); break;
      case 1: yield(); break;
    }
  }

  return c;
}

extern void __am_asm_trap(void);

bool cte_init(Context*(*handler)(Event, Context*)) {
  // initialize exception entry
  asm volatile("csrw mtvec, %0" : : "r"(__am_asm_trap));

  // register event handler
  user_handler = handler;

  return true;
}

Context *kcontext(Area kstack, void (*entry)(void *), void *arg) {
  return NULL;
}

void yield() {
  asm volatile("li a7, -1; ecall");
}

bool ienabled() {
  return false;
}

void iset(bool enable) {
}
