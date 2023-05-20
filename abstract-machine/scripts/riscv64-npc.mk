include $(AM_HOME)/scripts/isa/riscv64.mk
include $(AM_HOME)/scripts/platform/npc.mk
CFLAGS  += -DISA_H=\"riscv/riscv.h\"

AM_SRCS := riscv/npc/start.S \
           riscv/npc/trap.S \
           riscv/npc/trm.c \
           riscv/npc/ioe.c \
           riscv/npc/timer.c \
           riscv/npc/input.c \
           riscv/npc/gpu.c \
           riscv/npc/cte.c
