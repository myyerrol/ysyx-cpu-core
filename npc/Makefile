# 检查传递的类型是否在支持列表中
ifneq ($(MAKECMDGOALS), clean)
    PROJECTS = cpu_single_cycle cpu_yemu
    ifeq ($(filter $(PROJECTS), $(PROJECT)), )
        $(error Expected $$PROJECT in {$(PROJECTS)}, Got "$(PROJECT)")
    endif
endif

TOPNAME = Top

BUILD_DIR = ./build
BUILD_DIR_PROJECT =$(BUILD_DIR)/$(PROJECT)
OBJ_DIR_PROJECT = $(BUILD_DIR_PROJECT)/obj_dir
BIN_PROJECT = $(BUILD_DIR_PROJECT)/$(TOPNAME)

export PATH := $(PATH):$(abspath ./utils)

VERILATOR = verilator
VERILATOR_CFLAGS += -MMD --build -cc  \
                    -O3 --x-assign fast --x-initial fast --noassert --trace \
                    -Wno-WIDTHEXPAND \

-include $(NEMU_HOME)/include/config/auto.conf
-include $(NEMU_HOME)/include/config/auto.conf.cmd

INC_PATH += $(NEMU_HOME)/include
INC_PATH += $(NEMU_HOME)/include/memory
INC_PATH += $(NEMU_HOME)/src/isa/riscv64/include
INC_PATH += $(NEMU_HOME)/src/monitor/sdb

CFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\""
CFLAGS += -D__GUEST_ISA__=riscv64
CFLAGS += -g

LDFLAGS += -lreadline -ldl -pie

ifneq ($(MAKECMDGOALS), clean)
    PROJECT_SRCS_DIR = $(abspath $(PROJECT)/src)
    PROJECT_SRCV_DIR = $(abspath $(PROJECT)/srcv)
    PROJECT_SRCC_DIR = $(abspath $(PROJECT)/srcc)

    INC_PATH += $(PROJECT_SRCC_DIR)

    SRCSS_PROJECT     = $(shell find $(PROJECT_SRCS_DIR) -name "*.scala")
    SRCVS_PROJECT_GEN = $(BUILD_DIR_PROJECT)/Top.v
    SRCVS_PROJECT     = $(shell find $(PROJECT_SRCV_DIR) -name "*.v")
    SRCCS_PROJECT     = $(shell find $(PROJECT_SRCC_DIR) -name "*.c")
endif

INCFLAGS = $(addprefix -I, $(INC_PATH))

SRCCS_LIB += $(NEMU_HOME)/src/memory
# SRCCS_LIB += $(NEMU_HOME)/src/monitor/sdb/sdb.c $(NEMU_HOME)/src/monitor/sdb/expr.c
SRCCS_LIB += $(NEMU_HOME)/src/utils
SRCCS_PROJECT += $(shell find $(SRCCS_LIB) -name "*.c")

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

override ARGS ?= --log=$(BUILD_DIR)/npc-log.txt
IMG ?=
NPC_EXEC := $(BIN_PROJECT) $(ARGS) $(IMG)

.PHONY: test gen build run sim install_bsp reformat check_format clean

test:
	mill -i $(PROJECT).test
gen:  $(SRCVS_PROJECT_GEN)
	@echo "success"
run: $(BIN_PROJECT)
	rm -rf $(BUILD_DIR_PROJECT)/Wave.vcd
	$(NPC_EXEC)
gdb: $(BIN_PROJECT)
	rm -rf $(BUILD_DIR_PROJECT)/Wave.vcd
	gdb -s $(BIN_PROJECT) --args $(NPC_EXEC)
sim: $(BIN_PROJECT)
	$(call git_commit, "sim RTL")
	gtkwave $(BUILD_DIR_PROJECT)/Wave.vcd
	@echo "success"
install_bsp:
	mill -i mill.bsp.BSP/install
reformat:
	mill -i __.reformat
check_format:
	mill -i __.checkFormat
clean:
	rm -rf $(BUILD_DIR_PROJECT)
	rm -rf out
	rm -rf test_run_dir

include ../Makefile
