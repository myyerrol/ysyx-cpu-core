ifeq ($(wildcard $(NPC_HOME)/emu/src/main.c),)
    $(error NPC_HOME=$(NPC_HOME) is not a NPC repo)
endif

-include $(NPC_HOME)/emu/include/config/auto.conf
-include $(NPC_HOME)/emu/include/config/auto.conf.cmd

export PATH := $(PATH):$(abspath ./utils)

TOPNAME = Top
PROJECT = cpu

REMOVE_QUOTE = $(patsubst "%",%,$(1))
GUEST_ISA ?= $(call REMOVE_QUOTE,$(CONFIG_ISA))
ENGINE ?= $(call REMOVE_QUOTE,$(CONFIG_ENGINE))
NAME    = $(GUEST_ISA)-npc-$(ENGINE)

BUILD_DIR = $(NPC_HOME)/build
BUILD_DIR_PROJECT = $(BUILD_DIR)/$(PROJECT)
OBJ_DIR_PROJECT = $(BUILD_DIR_PROJECT)/obj_dir
BIN_PROJECT = $(BUILD_DIR_PROJECT)/$(NAME)

VERILATOR = verilator
VERILATOR_CFLAGS += -MMD --build -cc \
                    -O3 --x-assign fast --x-initial fast --noassert \
                    --trace \
                    -Wno-WIDTHEXPAND

INC_PATH += $(NPC_HOME)/emu/include \
            $(NPC_HOME)/emu/include/generated
INCFLAGS = $(addprefix -I, $(INC_PATH))

CFLAGS_BUILD += $(call REMOVE_QUOTE,$(CONFIG_CC_OPT))
CFLAGS_BUILD += $(if $(CONFIG_CC_LTO),-flto,)
CFLAGS_BUILD += $(if $(CONFIG_CC_DEBUG),-Og -ggdb3,)
CFLAGS_BUILD += $(if $(CONFIG_CC_ASAN),-fsanitize=address,)
CFLAGS  += $(CFLAGS_BUILD) \
           -D__GUEST_ISA__=$(GUEST_ISA) \
           -Wno-unused-result \
           -DTOP_NAME="\"V$(TOPNAME)\"" \
           $(INCFLAGS) \
           $(shell llvm-config-11 --cxxflags) -fPIE

LDFLAGS += $(CFLAGS_BUILD) \
           -lreadline -ldl -pie \
           $(shell llvm-config-11 --libs) \
           -lSDL2

PROJECT_SRCS_DIR = $(abspath $(NPC_HOME)/$(PROJECT)/src)
PROJECT_SRCV_DIR = $(abspath $(NPC_HOME)/$(PROJECT)/srcv)

SRCSS_PROJECT     = $(shell find $(PROJECT_SRCS_DIR) -name "*.scala")
SRCVS_PROJECT_GEN = $(BUILD_DIR_PROJECT)/Top.v
SRCVS_PROJECT     = $(shell find $(PROJECT_SRCV_DIR) -name "*.v")

SRCCS_DIR += $(NPC_HOME)/emu/src \
             $(NPC_HOME)/emu/src/cpu \
             $(NPC_HOME)/emu/src/debug \
             $(NPC_HOME)/emu/src/device \
             $(NPC_HOME)/emu/src/isa \
             $(NPC_HOME)/emu/src/memory \
             $(NPC_HOME)/emu/src/monitor \
             $(NPC_HOME)/emu/src/monitor/sdb \
             $(NPC_HOME)/emu/src/utils \

SRCCS_DIR_BLACKLIST +=
SRCCS_SRC_BLACKLIST +=
SRCCS_BLACKLIST += $(SRCCS_SRC_BLACKLIST) \
                   $(shell find $(SRCCS_DIR_BLACKLIST) -name "*.c")
SRCCS_WHITELIST += $(shell find $(SRCCS_DIR) -name "*.c")
SRCCS_PROJECT = $(filter-out $(SRCCS_BLACKLIST),$(SRCCS_WHITELIST)) \
                $(NPC_HOME)/emu/src/utils/disasm.cc

$(SRCVS_PROJECT_GEN): $(SRCSS_PROJECT)
	$(call git_commit, "generate verilog")
	mkdir -p $(BUILD_DIR_PROJECT)
	mill -i $(PROJECT).test.runMain TopMain -td $(BUILD_DIR_PROJECT)
$(BIN_PROJECT): $(SRCVS_PROJECT_GEN) $(SRCVS_PROJECT) $(SRCCS_PROJECT)
	@rm -rf $(OBJ_DIR_PROJECT)
	$(VERILATOR) $(VERILATOR_CFLAGS) \
	--top-module $(TOPNAME) $^ \
	$(addprefix -CFLAGS , $(CFLAGS)) \
	$(addprefix -LDFLAGS , $(LDFLAGS)) \
	--Mdir $(OBJ_DIR_PROJECT) --exe -o \
	$(abspath $(BIN_PROJECT))

ifdef CONFIG_DIFFTEST
ARGS_DIFF = --diff=$(NEMU_HOME)/build/riscv64-nemu-interpreter-so
endif

override ARGS ?= --log=$(BUILD_DIR)/npc-log.txt
override ARGS += $(ARGS_DIFF)

IMG ?=
NPC_EXEC := $(BIN_PROJECT) $(ARGS) $(IMG)

.PHONY: test gen build run sim install_bsp reformat check_format clean

test:
	mill -i $(PROJECT).test
gen: $(SRCVS_PROJECT_GEN)
	@echo "success"
run: $(BIN_PROJECT)
	$(call git_commit, "run NPC")
	rm -rf $(BUILD_DIR_PROJECT)/Wave.vcd
	$(NPC_EXEC)
gdb: $(BIN_PROJECT)
	$(call git_commit, "gdb NPC")
	rm -rf $(BUILD_DIR_PROJECT)/Wave.vcd
	gdb -s $(BIN_PROJECT) --args $(NPC_EXEC)
sim: $(BIN_PROJECT)
	$(call git_commit, "sim NPC")
	gtkwave $(BUILD_DIR_PROJECT)/Wave.vcd
	@echo "success"
install_bsp:
	mill -i mill.bsp.BSP/install
reformat:
	mill -i __.reformat
check_format:
	mill -i __.checkFormat
clean:
	rm -rf build
	rm -rf out
	rm -rf test_run_dir
