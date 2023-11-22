PROJECT      ?=
PROJECT_TEMP := single
PROJECT_LIST := single pipeline multip

ifeq ($(filter $(PROJECT_LIST), $(PROJECT)),)
    ifeq ($(findstring $(MAKECMDGOALS), config|clean),)
        $(error $$PROJECT is incorrect, optional values in [$(PROJECT_LIST)])
    endif
else
    PROJECT_TEMP = $(PROJECT)
endif

ifeq ($(wildcard $(NPC_HOME)/emu/src/main.c),)
    $(error NPC_HOME=$(NPC_HOME) is not a NPC repo)
endif

-include $(NPC_HOME)/emu/include/config/auto.conf
-include $(NPC_HOME)/emu/include/config/auto.conf.cmd

export PATH := $(PATH):$(abspath ./utils)

TOPNAME = Top

REMOVE_QUOTE = $(patsubst "%",%,$(1))
GUEST_ISA ?= $(call REMOVE_QUOTE,$(CONFIG_ISA))
ENGINE ?= $(call REMOVE_QUOTE,$(CONFIG_ENGINE))
NAME = $(GUEST_ISA)-npc-$(ENGINE)

BUILD_DIR = $(NPC_HOME)/cpu/build
BUILD_DIR_PROJECT = $(BUILD_DIR)/$(PROJECT_TEMP)
OBJ_DIR_PROJECT = $(BUILD_DIR_PROJECT)/obj_dir
BIN_PROJECT = $(BUILD_DIR_PROJECT)/$(NAME)

VERILATOR = verilator
VERILATOR_CFLAGS += -MMD --build -cc \
                    -O3 --x-assign fast --x-initial fast --noassert \
                    --trace \
                    -Wno-WIDTHEXPAND \
                    -Wno-WIDTHTRUNC

INC_PATH += $(NPC_HOME)/emu/include \
            $(NPC_HOME)/emu/include/generated
INCFLAGS = $(addprefix -I, $(INC_PATH))

CFLAGS_BUILD += $(call REMOVE_QUOTE,$(CONFIG_CC_OPT))
CFLAGS_BUILD += $(if $(CONFIG_CC_LTO),-flto,)
CFLAGS_BUILD += $(if $(CONFIG_CC_DEBUG),-Og -ggdb3,)
CFLAGS_BUILD += $(if $(CONFIG_CC_ASAN),-fsanitize=address,)
CFLAGS_BUILD += -DCPU_SINGLE=$(if $(PROJECT EQUAL single),true,false)
CFLAGS  += $(CFLAGS_BUILD) \
           -D__GUEST_ISA__=$(GUEST_ISA) \
           -Wno-unused-result \
           -DTOP_NAME="\"V$(TOPNAME)\"" \
           $(INCFLAGS) \
           $(shell llvm-config-11 --cxxflags) -fPIE

LDFLAGS += $(CFLAGS_BUILD) \
           -lreadline -ldl -pie \
           $(shell llvm-config-11 --libs) \
           -lSDL2

PROJECT_SRCS_DIR = $(abspath $(NPC_HOME)/cpu/$(PROJECT_TEMP)/src)
PROJECT_SRCV_DIR = $(abspath $(NPC_HOME)/cpu/$(PROJECT_TEMP)/srcv)

SRCSS_PROJECT     = $(shell find $(PROJECT_SRCS_DIR) -name "*.scala")
SRCVS_PROJECT_GEN = $(BUILD_DIR_PROJECT)/$(TOPNAME).v
SRCVS_PROJECT     = $(shell find $(PROJECT_SRCV_DIR) -name "*.v")

SRCCS_DIR += $(NPC_HOME)/emu/src \
             $(NPC_HOME)/emu/src/cpu \
             $(NPC_HOME)/emu/src/debug \
             $(NPC_HOME)/emu/src/device \
             $(NPC_HOME)/emu/src/isa \
             $(NPC_HOME)/emu/src/memory \
             $(NPC_HOME)/emu/src/monitor \
             $(NPC_HOME)/emu/src/monitor/sdb \
             $(NPC_HOME)/emu/src/utils \

SRCCS_DIR_BLACKLIST +=
SRCCS_SRC_BLACKLIST +=
SRCCS_BLACKLIST += $(SRCCS_SRC_BLACKLIST) \
                   $(shell find $(SRCCS_DIR_BLACKLIST) -name "*.c")
SRCCS_WHITELIST += $(shell find $(SRCCS_DIR) -name "*.c")
SRCCS_PROJECT = $(filter-out $(SRCCS_BLACKLIST),$(SRCCS_WHITELIST)) \
                $(NPC_HOME)/emu/src/utils/disasm.cc
SRCS = $(SRCVS_PROJECT_GEN) $(SRCVS_PROJECT) $(SRCCS_PROJECT)

CONFIG = $(NPC_HOME)/emu/.config

$(SRCVS_PROJECT_GEN): $(SRCSS_PROJECT)
	cd cpu && \
	mkdir -p $(BUILD_DIR_PROJECT) && \
	mill -i $(PROJECT_TEMP).test.runMain TopMain -td $(BUILD_DIR_PROJECT)
$(BIN_PROJECT): $(SRCS) $(CONFIG)
	@rm -rf $(OBJ_DIR_PROJECT)
	$(VERILATOR) $(VERILATOR_CFLAGS) \
	--top-module $(TOPNAME) $(SRCS) \
	$(addprefix -CFLAGS , $(CFLAGS)) \
	$(addprefix -LDFLAGS , $(LDFLAGS)) \
	--Mdir $(OBJ_DIR_PROJECT) --exe -o \
	$(abspath $(BIN_PROJECT))

ifdef CONFIG_DIFFTEST
    ARGS_DIFF = --diff=$(NEMU_HOME)/build/riscv64-nemu-interpreter-so
endif

override ARGS ?= --log=$(BUILD_DIR)/npc-log.txt
override ARGS += $(ARGS_DIFF)

IMG ?=
NPC_EXEC := $(BIN_PROJECT) $(ARGS) $(IMG)

.PHONY: test gen run gdb sim reformat checkformat clean

config:
	cd emu && make menuconfig

test:
	mill -i $(PROJECT_TEMP).test

gen: $(SRCVS_PROJECT_GEN)
	@echo "success"

run: $(BIN_PROJECT)
	$(call git_commit, "run NPC")
	rm -rf $(BUILD_DIR_PROJECT)/wave.vcd
	$(NPC_EXEC)

gdb: $(BIN_PROJECT)
	$(call git_commit, "gdb NPC")
	rm -rf $(BUILD_DIR_PROJECT)/wave.vcd
	gdb -s $(BIN_PROJECT) --args $(NPC_EXEC)

sim: $(BIN_PROJECT)
	$(call git_commit, "sim NPC")
	gtkwave $(BUILD_DIR_PROJECT)/wave.vcd
	@echo "success"

reformat:
	mill -i __.reformat

checkformat:
	mill -i __.checkFormat

clean:
	rm -rf cpu/build
	rm -rf cpu/out
	rm -rf cpu/test_run_dir
