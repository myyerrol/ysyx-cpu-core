#include <proc.h>
#include <elf.h>

#ifdef __LP64__
# define Elf_Ehdr Elf64_Ehdr
# define Elf_Phdr Elf64_Phdr
#else
# define Elf_Ehdr Elf32_Ehdr
# define Elf_Phdr Elf32_Phdr
#endif

extern size_t ramdisk_read(void *buf, size_t offset, size_t len);

static uintptr_t loader(PCB *pcb, const char *filename) {
  Elf_Ehdr elf_header;
  ramdisk_read(&elf_header, 0, sizeof(elf_header));
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

