#include <trace.h>

#define IRING_BUF_LEN 16
static char  *iringbuf[IRING_BUF_LEN];
static char **iringbuf_head = NULL;
static char **iringbuf_tail = iringbuf + (IRING_BUF_LEN - 1);
static char **iringbuf_curr = NULL;

void itrace_record(char *logbuf) {
  if (iringbuf_head == NULL) {
    iringbuf_head = iringbuf;
  }
  else {
    if (iringbuf_head != iringbuf_tail) {
      iringbuf_head++;
    }
    else {
      iringbuf_head = iringbuf;
    }
  }

  if (*iringbuf_head == NULL) {
    *iringbuf_head = (char *)malloc(sizeof(char) * 256);
  }
  strcpy(*iringbuf_head, logbuf);
  iringbuf_curr = iringbuf_head;
}

void itrace_display(char *type,
                    int inst_num,
                    char *op,
                    Decode *s,
                    int rd,
                    int rs1,
                    int rs2,
                    word_t src1,
                    word_t src2,
                    word_t imm,
                    word_t rd_val) {
  if (strcmp(type, "process") == 0) {
    _Log("[itrace] num:         %d\n", inst_num);
    _Log("[itrace] pc:          " FMT_WORD "\n", s->pc);
    _Log("[itrace] dnpc:        " FMT_WORD "\n", s->dnpc);
    _Log("[itrace] format hex:  " FMT_WORD "\n", (uint64_t)s->isa.inst.val);
    _Log("[itrace] format bin:  " PRINTF_BIN_PATTERN_INST "\n",
                                  PRINTF_BIN_INT32(s->isa.inst.val));
    _Log("[itrace] name:        %s\n",  op);
    _Log("[itrace] rs1 addr:    %d\n", rs1);
    _Log("[itrace] rs2 addr:    %d\n", rs2);
    _Log("[itrace] rd  addr:    %d\n", rd);
    _Log("[itrace] rs1 val hex: " FMT_WORD "\n", src1);
    _Log("[itrace] rs2 val hex: " FMT_WORD "\n", src2);
    // _Log("[itrace] rs1 val bin: " PRINTF_BIN_PATTERN_INT64 "\n",
    //       PRINTF_BIN_INT64(src1));
    // _Log("[itrace] rs2 val bin: " PRINTF_BIN_PATTERN_INT64 "\n",
    //       PRINTF_BIN_INT64(src2));
    _Log("[itrace] imm val hex: " FMT_WORD "\n", imm);
    // _Log("[itrace] imm val bin: " PRINTF_BIN_PATTERN_INT64 "\n",
    //       PRINTF_BIN_INT64(imm));
    _Log("[itrace] rd  val hex: " FMT_WORD "\n\n", rd_val);
  }
  else if (strcmp(type, "result") == 0) {
    iringbuf_head = iringbuf;
    while (*iringbuf_head != NULL && iringbuf_head != iringbuf_tail) {
      if (iringbuf_head == iringbuf_curr) {
        _Log("[itrace] ----> %s\n", *iringbuf_head);
      }
      else {
        _Log("[itrace]       %s\n", *iringbuf_head);
      }
      char *iringbuf_temp = *iringbuf_head;
      free(iringbuf_temp);
      iringbuf_head++;
    }
  }
}

void mtrace_display(char *type,
                    char *dir,
                    word_t addr,
                    word_t data,
                    word_t len) {
  if (strcmp(type, "process") == 0) {
    _Log("[mtrace] addr: " FMT_WORD " data: " FMT_WORD " %s\n", addr,
                                                                data,
                                                                dir);
  }
  else if (strcmp(type, "result") == 0) {
    word_t addr_base = (addr != 0) ? addr : CONFIG_MBASE;
    for (word_t i = 0; i < len; i++) {
      addr = addr_base + i * 4;
      data = paddr_read(addr, 8);
      _Log("[mtrace] addr: " FMT_WORD " data: " FMT_WORD "\n", addr, data);
    }
  }
}

#define ARR_LEN 1024 * 1024 * 100
#define JUDGE_LEN Assert( \
  offset < ARR_LEN, \
  "[ftrace] out of bounds: arr_len = %dKb, off_len = %ldKb", ARR_LEN, offset);
// 存储ELF符号表中的函数名称
static char  *func_name_arr[ARR_LEN];
// 存储指令调用和返回过程中的函数名称
static int    inst_func_call_depth = -1;
static char  *inst_func_name_arr[ARR_LEN];
static char **inst_func_name_head = inst_func_name_arr;
// 存储指令调用和返回过程中的调试信息
char  *ftrace_info_arr[ARR_LEN];
char **ftrace_info_head = ftrace_info_arr;

static int ftrace_is_elf_64(FILE *fp) {
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

static char *ftrace_get_func(Elf64_Addr addr) {
  Elf64_Addr offset = addr - CONFIG_MBASE;
  JUDGE_LEN;
  if (func_name_arr[offset] != NULL) {
    return func_name_arr[offset];
  }
  else {
    return (char *)"";
  }
}

void ftrace_init(const char *elf_file) {
  if (elf_file != NULL) {
    FILE *fp = fopen(elf_file, "r");
    Assert(fp, "Can not open '%s'", elf_file);
    Assert(ftrace_is_elf_64(fp), "File type mismatch");

    // 读取ELF文件头部
    Elf64_Ehdr elf_header;
    fread(&elf_header, 1, sizeof(elf_header), fp);

    // 读取ELF节区头部
    Elf64_Shdr elf_section_arr[elf_header.e_shnum];
    fseek(fp, elf_header.e_shoff, SEEK_SET);
    fread(elf_section_arr,
          sizeof(Elf64_Shdr),
          (elf_header.e_shnum * elf_header.e_shentsize),
          fp);

    // 读取ELF节区符号表和节区字符表
    Elf64_Shdr elf_section_symbol;
    Elf64_Shdr elf_section_string;
    for (int i = 0; i < elf_header.e_shnum; i++) {
      if (elf_section_arr[i].sh_type == SHT_SYMTAB) {
        memcpy(&elf_section_symbol, &elf_section_arr[i], sizeof(Elf64_Shdr));
        if (elf_section_arr[elf_section_symbol.sh_link].sh_type == SHT_STRTAB) {
          memcpy(&elf_section_string,
                 &elf_section_arr[elf_section_symbol.sh_link],
                 sizeof(Elf64_Shdr));
        }
      }
    }

    // 读取ELF字符表
    char elf_string_name_arr[elf_section_string.sh_size];
    fseek(fp, elf_section_string.sh_offset, SEEK_SET);
    fread(&elf_string_name_arr, 1, sizeof(elf_string_name_arr), fp);

    // 读取ELF符号表
    Elf64_Xword elf_symbol_cnt = elf_section_symbol.sh_size /
                                 elf_section_symbol.sh_entsize;
    Elf64_Sym elf_symbol_arr[elf_symbol_cnt];
    fseek(fp, elf_section_symbol.sh_offset, SEEK_SET);
    fread(&elf_symbol_arr, 1, elf_section_symbol.sh_size, fp);
    for (int i = 0; i < elf_symbol_cnt; i++) {
      uint8_t st_info = elf_symbol_arr[i].st_info;
      // 根据符号表名称偏移量读取字符表中的对应内容
      if (ELF64_ST_TYPE(st_info) == STT_FUNC) {
        Elf64_Addr st_value = elf_symbol_arr[i].st_value;
        Elf32_Word st_name = elf_symbol_arr[i].st_name;
        char *func_name = elf_string_name_arr + st_name;
        Elf64_Addr offset = st_value - elf_header.e_entry;
        JUDGE_LEN;
        if (func_name_arr[offset] == NULL) {
            func_name_arr[offset] = (char *)malloc(sizeof(char) * 256);
        }
        strcpy(func_name_arr[offset], func_name);
#ifdef CONFIG_FTRACE_ELF
        _Log("[ftrace] symb addr: " FMT_WORD "\n", st_value);
        _Log("[ftrace] func name:  %s\n\n", func_name);
#endif
      }
    }

    fclose(fp);
  }
}

void ftrace_display(char *type,
                    bool inst_func_call,
                    bool inst_func_ret,
                    word_t pc,
                    word_t dnpc) {
  if (strcmp(type, "result") == 0) {
    ftrace_info_head = ftrace_info_arr;
    while (*ftrace_info_head != NULL) {
      _Log("%s", *ftrace_info_head);
      ftrace_info_head++;
    }
    return;
  }

  char buf[1024];
  char buf_head[512];
  char buf_tail[512];

  // 如果程序在函数内部进行跳转，则不需要记录调用信息
  if (inst_func_call && strcmp(ftrace_get_func(dnpc), "") == 0) {
    return;
  }

  if (inst_func_call || inst_func_ret) {
    sprintf(buf_head, "[ftrace] addr: 0x%08" PRIx32, (uint32_t)pc);
  }

  if (inst_func_call) {
    inst_func_name_head++;
    inst_func_call_depth++;
    if (*inst_func_name_head == NULL) {
      *inst_func_name_head = (char *)malloc(sizeof(char *) * 256);
    }
    strcpy(*inst_func_name_head, ftrace_get_func(dnpc));
    char printf_format[] = " call %*s[%s@" "0x%08" PRIx32 "]\n";
    sprintf(buf_tail,
            printf_format,
            inst_func_call_depth * 2,
            "",
            *inst_func_name_head,
            (uint32_t)dnpc);
  }

  if (inst_func_ret) {
    inst_func_name_head--;
    inst_func_call_depth--;
    char printf_format[] = " ret  %*s[%s]\n";
    sprintf(buf_tail,
            printf_format,
            inst_func_call_depth * 2,
            "",
            *inst_func_name_head);
  }

  if (inst_func_call || inst_func_ret) {
    sprintf(buf, "%s%s", buf_head, buf_tail);
    if (*ftrace_info_head == NULL) {
      *ftrace_info_head = (char *)malloc(sizeof(char *) * 1024);
    }
    strcpy(*ftrace_info_head, buf);
    ftrace_info_head++;

    if (strcmp(type, "process") == 0) {
      _Log("%s", buf);
    }
  }
}

void ftrace_free() {
  for (int i = 0; i < ARR_LEN; i++) {
    if (func_name_arr[i] != NULL) {
      free(func_name_arr[i]);
    }
    if (ftrace_info_arr[i] != NULL) {
      free(ftrace_info_arr[i]);
    }
    if (inst_func_name_arr[i] != NULL) {
      free(inst_func_name_arr[i]);
    }
  }
}

void dtrace_display(char *type,
                    char *dir,
                    const char *name,
                    word_t addr,
                    word_t data) {
  if (strcmp(type, "process") == 0) {
    _Log("[dtrace] addr: " FMT_WORD " data: " FMT_WORD " %s %s\n", addr,
                                                                   data,
                                                                   dir,
                                                                   name);
  }
  else if (strcmp(type, "result") == 0) {
  }
}
