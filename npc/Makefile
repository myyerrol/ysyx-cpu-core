BUILD_DIR = ./build
BUILD_DIR_CPU_YEMU = $(BUILD_DIR)/cpu_yemu
BUILD_DIR_CPU_SINGLE_CYCLE = $(BUILD_DIR)/cpu_single_cycle
BUILD_DIR_CPU_PIPELINE = $(BUILD_DIR)/cpu_pipeline

export PATH := $(PATH):$(abspath ./utils)

test_cpu_yemu:
	mill -i cpu_yemu.test
test_cpu_single_cycle:
	mill -i cpu_single_cycle.test
test_cpu_pipeline:
	mill -i cpu_pipeline.test
verilog_cpu_yemu:
	$(call git_commit, "generate verilog")
	mkdir -p $(BUILD_DIR_CPU_YEMU)
	mill -i cpu_yemu.test.runMain TopMain -td $(BUILD_DIR_CPU_YEMU)
verilog_cpu_single_cycle:
	$(call git_commit, "generate verilog")
	mkdir -p $(BUILD_DIR_CPU_SINGLE_CYCLE)
	mill -i cpu_single_cycle.test.runMain TopMain -td $(BUILD_DIR_CPU_SINGLE_CYCLE)
verilog_cpu_pipeline:
	$(call git_commit, "generate verilog")
	mkdir -p $(BUILD_DIR_CPU_PIPELINE)
	mill -i cpu_pipeline.test.runMain TopMain -td $(BUILD_DIR_CPU_PIPELINE)
help:
	mill -i __.test.runMain TopMain --help
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
sim:
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "Write this Makefile by yourself."

.PHONY: test_cpu_yemu test_cpu_single_cycle test_cpu_pipeline \
		verilog_cpu_yemu verilog_cpu_single_cycle verilog_cpu_pipeline \
	    help compile bsp reformat checkformat clean sim

include ../Makefile
