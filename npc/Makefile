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
	rm -rf obj_dir

# Verilator
ifneq ($(words $(CURDIR)), 1)
    $(error Unsupported: GNU Make cannot build in directories containing spaces, build elsewhere: '$(CURDIR)')
endif

ifeq ($(VERILATOR_ROOT),)
    VERILATOR = verilator
else
    export VERILATOR_ROOT
    VERILATOR = $(VERILATOR_ROOT)/bin/verilator
endif

# VSRCS = $(shell find $(abspath ./build/cpu_single_cycle}) -name "*.v")

build_cpu_single_cycle:
	$(VERILATOR) -cc --exe --build -j ${BUILD_DIR_CPU_SINGLE_CYCLE}/Top.v \
	cpu_single_cycle/cpp/Top.cpp -o Top
run_cpu_single_cycle:
	obj_dir/Top

sim:
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "Write this Makefile by yourself."



include ../Makefile
