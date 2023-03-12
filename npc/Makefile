BUILD_DIR = ./build
BUILD_DIR_CPU_YEMU = $(BUILD_DIR)/cpu_yemu
BUILD_DIR_CPU_SINGLE_CYCLE = $(BUILD_DIR)/cpu_single_cycle
BUILD_DIR_CPU_PIPELINE = $(BUILD_DIR)/cpu_pipeline

export PATH := $(PATH):$(abspath ./utils)

.PHONY: test_cpu_yemu test_cpu_single_cycle test_cpu_pipeline \
		gen_cpu_yemu gen_cpu_single_cycle gen_cpu_pipeline \
	    compile bsp reformat checkformat clean sim

# Chisel
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
gen_cpu_single_cycle:
	$(call git_commit, "generate verilog")
	mkdir -p $(BUILD_DIR_CPU_SINGLE_CYCLE)
	mill -i cpu_single_cycle.test.runMain TopMain -td $(BUILD_DIR_CPU_SINGLE_CYCLE)
gen_cpu_pipeline:
	$(call git_commit, "generate verilog")
	mkdir -p $(BUILD_DIR_CPU_PIPELINE)
	mill -i cpu_pipeline.test.runMain TopMain -td $(BUILD_DIR_CPU_PIPELINE)
compile:
	mill -i __.compile
bsp:
	mill -i mill.bsp.BSP/install
reformat:
	mill -i __.reformat
checkformat:
	mill -i __.checkFormat
clean:
	rm -rf $(BUILD_DIR)
	rm -rf out
	rm -rf test_run_dir

# Verilator
TOPNAME = Top
INC_PATH += $(NEMU_HOME)/include

VERILATOR = verilator
VERILATOR_CFLAGS += -MMD --build -cc  \
                    -O3 --x-assign fast --x-initial fast --noassert

# BUILD_DIR = $(BUILD_DIR_CPU_SINGLE_CYCLE)
OBJ_DIR = $(BUILD_DIR_CPU_SINGLE_CYCLE)/obj_dir
BIN = $(BUILD_DIR_CPU_SINGLE_CYCLE)/$(TOPNAME)

$(shell mkdir -p $(BUILD_DIR_CPU_SINGLE_CYCLE))

VSRCS += $(shell find $(abspath ./) -name "*.v")
CSRCS += $(shell find $(abspath ./) -name "*.c" -or -name "*.cc" -or -name "*.cpp")

INCFLAGS = $(addprefix -I, $(INC_PATH))
CFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\""

111: $(VSRCS) $(CSRCS)
	@rm -rf $(OBJ_DIR)
	$(VERILATOR) $(VERILATOR_CFLAGS) \
		--top-module $(TOPNAME) $^ \
		$(addprefix -CFLAGS , $(CFLAGS)) \
		--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN))

sim:
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "Write this Makefile by yourself."

include ../Makefile
