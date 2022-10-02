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
	clean

default: build_top

build_top:
	$(VERILATOR) -cc --exe --build -j top.v top.cpp -o top

build_top_vcd:
	$(VERILATOR) -cc --exe --build -j top.v top_vcd.cpp -o top_vcd --trace

run_top:
	obj_dir/top

run_top_vcd:
	obj_dir/top_vcd

run_sim:
	gtkwave wave.vcd

clean:
	-rm -rf obj_dir
