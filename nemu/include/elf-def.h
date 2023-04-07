#ifndef __ELF_H__
#define __ELF_H__

#include <elf.h>

char *elf_get_func(Elf64_Addr addr);
void elf_free();

#endif
