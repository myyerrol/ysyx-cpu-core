
#include <elf.h>
#include <stdio.h>
#include <string.h>

#include <common.h>

typedef struct {
    char*      name;
    Elf64_Shdr shdr;
} ElfSectionMap;

int is_elf_64(FILE *fp) {
    char buf[16];
    int  nread = fread(buf, 1, 16, fp);
    fseek(fp, 0, SEEK_SET);
    // 如果读到的数据小于16位则不是ELF64格式文件
    if (nread < 16) {
        return 0;
    }
    // 如果前4个字符存在不同则不是ELF64格式文件
    if (strncmp(buf, ELFMAG, SELFMAG)) {
        return 0;
    }
    // 如果缓存数据与标准不等则不是ELF64格式文件
    if (buf[EI_CLASS] != ELFCLASS64) {
        return 0;
    }

    return 1;
}

void init_elf(const char *elf_file) {
    if (elf_file != NULL) {
        FILE *fp = fopen(elf_file, "r");
        Assert(fp, "Can not open '%s'", elf_file);
        Assert(is_elf_64(fp), "File type mismatch");

        // 读取ELF文件头部
        Elf64_Ehdr elf_headers;
        fread(&elf_headers, 1, sizeof(elf_headers), fp);

        // 读取ELF节区头部
        Elf64_Shdr elf_sections[elf_headers.e_shnum];
        fseek(fp, elf_headers.e_shoff, SEEK_SET);
        fread(elf_sections,
              sizeof(Elf64_Shdr),
              (elf_headers.e_shnum * elf_headers.e_shentsize),
              fp);

        // 读取ELF节区符号表和节区字符表
        Elf64_Shdr elf_sections_symbol;
        Elf64_Shdr elf_sections_string;
        for (int i = 0; i < elf_headers.e_shnum; i++) {
            if (elf_sections[i].sh_type == SHT_SYMTAB) {
                memcpy(&elf_sections_symbol,
                       &elf_sections[i],
                       sizeof(Elf64_Shdr));
                if (elf_sections[elf_sections_symbol.sh_link].sh_type ==
                    SHT_STRTAB) {
                    memcpy(&elf_sections_string,
                           &elf_sections[elf_sections_symbol.sh_link],
                           sizeof(Elf64_Shdr));
                }
            }
        }

        // 读取ELF字符表
        char elf_strings_names[elf_sections_string.sh_size];
        fseek(fp, elf_sections_string.sh_offset, SEEK_SET);
        fread(&elf_strings_names, 1, sizeof(elf_strings_names), fp);

        // 读取ELF符号表
        Elf64_Xword elf_symbols_cnt = elf_sections_symbol.sh_size /
                                      elf_sections_symbol.sh_entsize;
        Elf64_Sym elf_symbols[elf_symbols_cnt];
        fseek(fp, elf_sections_symbol.sh_offset, SEEK_SET);
        fread(&elf_symbols, 1, elf_sections_symbol.sh_size, fp);
        for (int i = 0; i < elf_symbols_cnt; i++) {
            uint8_t st_info = elf_symbols[i].st_info;
            // 根据符号表名称偏移量读取字符表中的对应内容
            if (ELF64_ST_TYPE(st_info) == STT_FUNC) {
                Elf64_Addr st_value = elf_symbols[i].st_value;
                Elf32_Word st_name = elf_symbols[i].st_name;
                char *name = elf_strings_names + st_name;
                printf("Symbol Address: " FMT_WORD "\n", st_value);
                printf("Function Name:  %s\n\n", name);
            }
        }

        fclose(fp);
    }
}
