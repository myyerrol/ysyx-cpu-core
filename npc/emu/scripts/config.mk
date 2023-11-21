COLOR_RED := $(shell echo "\033[1;31m")
COLOR_END := $(shell echo "\033[0m")

ifeq ($(wildcard .config),)
    $(warning $(COLOR_RED)Warning: .config does not exists!$(COLOR_END))
    $(warning $(COLOR_RED)To build the project, first run 'make menuconfig'.$(COLOR_END))
endif

Q            := @
KCONFIG_PATH := $(NPC_HOME)/emu/tools/kconfig
FIXDEP_PATH  := $(NPC_HOME)/emu/tools/fixdep
KCONFIG      := $(NPC_HOME)/emu/configs/Kconfig
KCONFIG_GEN  += include/generated include/config .config .config.old
SILENT := -s

CONF   := $(KCONFIG_PATH)/build/conf
MCONF  := $(KCONFIG_PATH)/build/mconf
FIXDEP := $(FIXDEP_PATH)/build/fixdep

$(CONF):
	$(Q)$(MAKE) $(SILENT) -C $(KCONFIG_PATH) NAME=conf

$(MCONF):
	$(Q)$(MAKE) $(SILENT) -C $(KCONFIG_PATH) NAME=mconf

$(FIXDEP):
	$(Q)$(MAKE) $(SILENT) -C $(FIXDEP_PATH)

menuconfig: $(MCONF) $(CONF) $(FIXDEP)
	$(Q)$(MCONF) $(KCONFIG)
	$(Q)$(CONF) $(SILENT) --syncconfig $(KCONFIG)

savedefconfig: $(CONF)
	$(Q)$< $(SILENT) --$@=configs/defconfig $(KCONFIG)

%defconfig: $(CONF) $(FIXDEP)
	$(Q)$< $(SILENT) --defconfig=configs/$@ $(KCONFIG)
	$(Q)$< $(SILENT) --syncconfig $(KCONFIG)

.PHONY: menuconfig savedefconfig defconfig

help:
	@echo  'menuconfig    - Update current config utilising a menu based program'
	@echo  'savedefconfig - Save current config as configs/defconfig (minimal config)'

clean-configs: clean
	-@rm -rf $(KCONFIG_GEN)

.PHONY: help clean-configs

define call_fixdep
	@$(FIXDEP) $(1) $(2) unused > $(1).tmp
	@mv $(1).tmp $(1)
endef
