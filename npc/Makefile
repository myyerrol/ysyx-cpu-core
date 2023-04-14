# 检查传递的类型是否在支持列表中
PROJECTS = cpu_single_cycle cpu_yemu
ifeq ($(filter $(PROJECTS), $(PROJECT)), )
  $(error Expected $$PROJECT in {$(PROJECTS)}, Got "$(PROJECT)")
endif

BUILD_DIR = ./build
BUILD_DIR_PROJECT =$(BUILD_DIR)/$(PROJECT)

export PATH := $(PATH):$(abspath ./utils)

TOPNAME = Top
INC_PATH += $(NEMU_HOME)/include

VERILATOR = verilator
VERILATOR_CFLAGS += -MMD --build -cc  \
                    -O3 --x-assign fast --x-initial fast --noassert --trace

OBJ_DIR_PROJECT = $(BUILD_DIR_PROJECT)/obj_dir
BIN_PROJECT = $(BUILD_DIR_PROJECT)/$(TOPNAME)

INCFLAGS = $(addprefix -I, $(INC_PATH))
CFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\""

SRCS_DIR = /src
SRCV_DIR = /srcv
SRCC_DIR = /srcc

SRCSS_PROJECT     = $(shell find $(abspath $(PROJECT)$(SRCS_DIR)) -name "*.scala")
SRCVS_PROJECT_GEN = $(BUILD_DIR_PROJECT)/Top.v
SRCVS_PROJECT     = $(shell find $(abspath $(PROJECT)$(SRCV_DIR)) -name "*.v")
SRCCS_PROJECT     = $(shell find $(abspath $(PROJECT)$(SRCC_DIR)) -name "*.cpp")

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

.PHONY: test gen build run sim install_bsp reformat check_format clean

test:
	mill -i $(PROJECT).test
gen:  $(SRCVS_PROJECT_GEN)
	@echo "success"
run: $(BIN_PROJECT)
	rm -rf $(BUILD_DIR_PROJECT)/Wave.vcd
	$<
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
