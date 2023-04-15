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
                    -O3 --x-assign fast --x-initial fast --noassert --trace

-include $(NEMU_HOME)/include/config/auto.conf
-include $(NEMU_HOME)/include/config/auto.conf.cmd

INC_PATH += $(NEMU_HOME)/include
INC_PATH += $(NEMU_HOME)/include/memory
INC_PATH += $(NEMU_HOME)/src/isa/riscv64/include
INCFLAGS = $(addprefix -I, $(INC_PATH))

CFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\""
CFLAGS += -D__GUEST_ISA__=riscv64

SRCS_DIR = /src
SRCV_DIR = /srcv
SRCC_DIR = /srcc

SRCSS_PROJECT     = $(shell find $(abspath $(PROJECT)$(SRCS_DIR)) -name "*.scala")
SRCVS_PROJECT_GEN = $(BUILD_DIR_PROJECT)/Top.v
SRCVS_PROJECT     = $(shell find $(abspath $(PROJECT)$(SRCV_DIR)) -name "*.v")
SRCCS_PROJECT     = $(shell find $(abspath $(PROJECT)$(SRCC_DIR)) -name "*.c")

SRCCS_LIB += $(NEMU_HOME)/src/memory
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
	--Mdir $(OBJ_DIR_PROJECT) --exe -o \
	$(abspath $(BIN_PROJECT))

override ARGS ?= --log=$(BUILD_DIR)/npc-log.txt
IMG ?=

.PHONY: test gen build run sim install_bsp reformat check_format clean

test:
	mill -i $(PROJECT).test
gen:  $(SRCVS_PROJECT_GEN)
	@echo "success"
run: $(BIN_PROJECT)
	rm -rf $(BUILD_DIR_PROJECT)/Wave.vcd
	$(BIN_PROJECT) $(ARGS) $(IMG)
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
