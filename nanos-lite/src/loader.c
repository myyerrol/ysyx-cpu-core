#include <proc.h>
#include <elf.h>

#ifdef __LP64__
# define Elf_Ehdr Elf64_Ehdr
# define Elf_Phdr Elf64_Phdr
#else
# define Elf_Ehdr Elf32_Ehdr
# define Elf_Phdr Elf32_Phdr
#endif

#if defined(__ISA_AM_NATIVE__)
#define EXPECT_TYPE EM_X86_64
#elif defined(__ISA_X86__)
#define EXPECT_TYPE EM_X86_64
#elif defined(__ISA_MIPS32__)
#define EXPECT_TYPE EM_MIPS
#elif defined(__ISA_RISCV32__) || defined(__ISA_RISCV64__)
#define EXPECT_TYPE EM_RISCV
#else
#error Unsupported ISA
#endif

extern size_t ramdisk_read(void *buf, size_t offset, size_t len);

static uintptr_t loader(PCB *pcb, const char *filename) {
  Elf_Ehdr elf_header;
  ramdisk_read(&elf_header, 0, sizeof(elf_header));

  assert(*(uint32_t *)elf_header.e_ident == 0x464C457f);
  assert(elf_header.e_machine == EXPECT_TYPE);

  Elf_Phdr elf_segment_arr[elf_header.e_phnum];
  ramdisk_read(elf_segment_arr,
               elf_header.e_phoff,
               sizeof(Elf_Phdr) * elf_header.e_phnum);
  for (int i = 0; i < elf_header.e_phnum; i++) {
    if (elf_segment_arr[i].p_type == PT_LOAD) {
      ramdisk_read((void *)elf_segment_arr[i].p_vaddr,
                           elf_segment_arr[i].p_offset,
                           elf_segment_arr[i].p_memsz);
      memset((void *)(elf_segment_arr[i].p_vaddr + elf_segment_arr[i].p_filesz),
             0,
             elf_segment_arr[i].p_memsz - elf_segment_arr[i].p_filesz);
    }
  }

  return elf_header.e_entry;
}

void naive_uload(PCB *pcb, const char *filename) {
  uintptr_t entry = loader(pcb, filename);
  Log("Jump to entry = %p", entry);
  ((void(*)())entry) ();
}

