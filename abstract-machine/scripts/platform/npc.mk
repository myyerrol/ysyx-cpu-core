AM_SRCS := platform/dummy/vme.c \
           platform/dummy/mpe.c

CFLAGS     += -fdata-sections -ffunction-sections
LDFLAGS    += -T $(AM_HOME)/scripts/linker.ld \
             --defsym=_pmem_start=0x80000000 --defsym=_entry_offset=0x0
LDFLAGS    += --gc-sections -e _start
ARGS_EMBED += -l $(shell dirname $(IMAGE).elf)/npc-log.txt
ARGS_EMBED += -e $(IMAGE).elf

CFLAGS += -DMAINARGS=\"$(mainargs)\"
.PHONY: $(AM_HOME)/am/src/riscv/npc/trm.c

image: $(IMAGE).elf
	@$(OBJDUMP) -d $(IMAGE).elf > $(IMAGE).txt
	@echo + OBJCOPY "->" $(IMAGE_REL).bin
	@$(OBJCOPY) -S --set-section-flags .bss=alloc,contents -O binary $(IMAGE).elf $(IMAGE).bin

run: image
	$(MAKE) -C $(NPC_HOME) run ARGS="$(ARGS_EMBED) $(ARGS)" IMG=$(IMAGE).bin PROJECT=$(PROJECT)

gdb: image
	$(MAKE) -C $(NPC_HOME) gdb ARGS="$(ARGS_EMBED) $(ARGS)" IMG=$(IMAGE).bin PROJECT=$(PROJECT)
