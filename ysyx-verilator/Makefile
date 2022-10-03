ifneq ($(words $(CURDIR)), 1)
    $(error Unsupported: GNU Make cannot build in directories containing spaces, build elsewhere: '$(CURDIR)')
endif

ifeq ($(VERILATOR_ROOT),)
    VERILATOR = verilator
else
    export VERILATOR_ROOT
    VERILATOR = $(VERILATOR_ROOT)/bin/verilator
endif

.PHONY:
	default clean

default: build_switch

build_switch:
	$(VERILATOR) -cc --exe --build -j switch.v switch.cpp -o switch

build_switch_vcd:
	$(VERILATOR) -cc --exe --build -j switch.v switch_vcd.cpp -o switch_vcd --trace

run_switch:
	obj_dir/switch

run_switch_vcd:
	obj_dir/switch_vcd

run_sim:
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	gtkwave wave.vcd

clean:
	-rm -rf obj_dir

include ../Makefile
