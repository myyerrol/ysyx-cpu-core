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

default:
	$(VERILATOR) -cc --exe --build -j top.v top_main.cpp

run:
	obj_dir/Vtop

clean:
	-rm -rf obj_dir
