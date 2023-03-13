BUILD_DIR = ./build
BUILD_DIR_CPU_YEMU = $(BUILD_DIR)/cpu_yemu
BUILD_DIR_CPU_SINGLE_CYCLE = $(BUILD_DIR)/cpu_single_cycle
BUILD_DIR_CPU_PIPELINE = $(BUILD_DIR)/cpu_pipeline

export PATH := $(PATH):$(abspath ./utils)

TOPNAME = Top
INC_PATH += $(NEMU_HOME)/include

VERILATOR = verilator
VERILATOR_CFLAGS += -MMD --build -cc  \
                    -O3 --x-assign fast --x-initial fast --noassert

OBJ_DIR_CPU_SINGLE_CYCLE = $(BUILD_DIR_CPU_SINGLE_CYCLE)/obj_dir
BIN_CPU_SINGLE_CYCLE = $(BUILD_DIR_CPU_SINGLE_CYCLE)/$(TOPNAME)

INCFLAGS = $(addprefix -I, $(INC_PATH))
CFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\""

SSRCS_CPU_SINGLE_CYCLE = $(shell find $(abspath "./cpu_single_cycle/src") -name "*.scala")
VSRCS_CPU_SINGLE_CYCLE = $(BUILD_DIR_CPU_SINGLE_CYCLE)/Top.v
CSRCS_CPU_SINGLE_CYCLE = $(shell find $(abspath "./cpu_single_cycle/cpp") -name "*.cpp")

$(VSRCS_CPU_SINGLE_CYCLE): $(SSRCS_CPU_SINGLE_CYCLE)
	$(call git_commit, "generate verilog")
	mkdir -p $(BUILD_DIR_CPU_SINGLE_CYCLE)
	mill -i cpu_single_cycle.test.runMain TopMain -td $(BUILD_DIR_CPU_SINGLE_CYCLE)
$(BIN_CPU_SINGLE_CYCLE): $(VSRCS_CPU_SINGLE_CYCLE) $(CSRCS_CPU_SINGLE_CYCLE)
	@rm -rf $(OBJ_DIR_CPU_SINGLE_CYCLE)
	$(VERILATOR) $(VERILATOR_CFLAGS) \
	--top-module $(TOPNAME) $^ \
	$(addprefix -CFLAGS , $(CFLAGS)) \
	--Mdir $(OBJ_DIR_CPU_SINGLE_CYCLE) --exe -o \
	$(abspath $(BIN_CPU_SINGLE_CYCLE))

.PHONY: test_cpu_yemu test_cpu_single_cycle test_cpu_pipeline \
		gen_cpu_yemu gen_cpu_single_cycle gen_cpu_pipeline \
		build_cpu_single_cycle \
		run_cpu_single_cycle \
		sim \
	    install_bsp reformat check_format clean

test_cpu_yemu:
	mill -i cpu_yemu.test
test_cpu_single_cycle:
	mill -i cpu_single_cycle.test
test_cpu_pipeline:
	mill -i cpu_pipeline.test
gen_cpu_yemu:
	$(call git_commit, "generate verilog")
	mkdir -p $(BUILD_DIR_CPU_YEMU)
	mill -i cpu_yemu.test.runMain TopMain -td $(BUILD_DIR_CPU_YEMU)
gen_cpu_single_cycle:  $(VSRCS_CPU_SINGLE_CYCLE)
	@echo "success"
gen_cpu_pipeline:
	$(call git_commit, "generate verilog")
	mkdir -p $(BUILD_DIR_CPU_PIPELINE)
	mill -i cpu_pipeline.test.runMain TopMain -td $(BUILD_DIR_CPU_PIPELINE)
build_cpu_single_cycle: $(BIN_CPU_SINGLE_CYCLE)
	@echo "success"
run_cpu_single_cycle: $(BIN_CPU_SINGLE_CYCLE)
	$<
sim:
	$(call git_commit, "sim RTL")
	@echo "success"

install_bsp:
	mill -i mill.bsp.BSP/install
reformat:
	mill -i __.reformat
check_format:
	mill -i __.checkFormat
clean:
	rm -rf $(BUILD_DIR)
	rm -rf out
	rm -rf test_run_dir

include ../Makefile
