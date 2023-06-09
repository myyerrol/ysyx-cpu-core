#include <common.h>
#include <fs.h>

#include "syscall.h"

#define STRACE_COND_PROCESS

intptr_t sys_write(int fd, const void *buf, size_t len) {
  intptr_t i = 0;
  if (fd == 1 || fd == 2) {
    for(; len > 0; len--) {
      putch(((char*)buf)[i]);
      i++;
    }
    return i;
  }
  else {
    return -1;
  }
}

void do_syscall(Context *c) {
  intptr_t a[4];
  a[0] = c->GPR1;
  a[1] = c->GPR2;
  a[2] = c->GPR3;
  a[3] = c->GPR4;

  switch (a[0]) {
    case SYS_exit: {
      c->GPRx = c->GPR2;
      halt(c->GPR2);
      break;
    }
    case SYS_yield: {
      c->GPRx = 0;
      yield();
      break;
    }
    case SYS_open: {
      c->GPRx = fs_open((char *)a[1], a[2], a[3]);
      break;
    }
    case SYS_read: {
      c->GPRx = fs_read(a[1], (void *)a[2], a[3]);
      break;
    }
    case SYS_write: {
      // c->GPRx = sys_write(a[1], (void *)a[2], a[3]);
      c->GPRx = fs_write(a[1], (void *)a[2], a[3]);
      break;
    }
    case SYS_close: {
      c->GPRx = fs_close(a[1]);
      break;
    }
    case SYS_lseek: {
      c->GPRx = fs_lseek(a[1], a[2], a[3]);
      break;;
    }
    case SYS_brk: {
      c->GPRx = 0;
      break;
    }
    default: {
      panic("Unhandled syscall ID = %d", a[0]);
      break;
    }
  }

#ifdef STRACE_COND_PROCESS
  char *type = (a[0] ==  SYS_exit) ? " SYS_EXIT" :
               (a[0] == SYS_yield) ? "SYS_YIELD" :
               (a[0] ==  SYS_open) ? " SYS_OPEN" :
               (a[0] ==  SYS_read) ? " SYS_READ" :
               (a[0] == SYS_write) ? "SYS_WRITE" :
               (a[0] == SYS_close) ? "SYS_CLOSE" :
               (a[0] == SYS_lseek) ? "SYS_LSEEK" :
               (a[0] ==   SYS_brk) ? "  SYS_BRK" : "";
  printf("[strace] type: %s, a0 = %x, a1 = %x, a2 = %x, ret: %x\n", type,
                                                                    a[1],
                                                                    a[2],
                                                                    a[3],
                                                                    c->GPRx);
#endif
}
