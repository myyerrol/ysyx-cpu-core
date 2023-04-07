/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include "local-include/reg.h"
#include <cpu/cpu.h>
#include <cpu/ifetch.h>
#include <cpu/decode.h>
#include <elf-def.h>

#define R(i) gpr(i)
#define Mr vaddr_read
#define Mw vaddr_write

enum {
  TYPE_R,
  TYPE_I,
  TYPE_S,
  TYPE_B,
  TYPE_U,
  TYPE_J,
  TYPE_N
};

#define src1R() do { *src1 = R(*rs1); } while (0);
#define src2R() do { *src2 = R(*rs2); } while (0);
#define immI() do { *imm = SEXT(BITS(i, 31, 20), 12); } while (0);
#define immS() do { *imm = (SEXT(BITS(i, 31, 25), 7) << 5) | \
                                 BITS(i, 11, 7); } while (0);
#define immB() do { *imm = SEXT(((BITS(i, 31, 31) << 11) | \
                                 (BITS(i, 7, 7) << 10) | \
                                 (BITS(i, 30, 25) << 4) | \
                                  BITS(i, 11, 8)), 12) << 1; } while (0);
#define immU() do { *imm = SEXT(BITS(i, 31, 12), 20) << 12; } while (0);
#define immJ() do { *imm = SEXT(((BITS(i, 20, 20) << 19) | \
                                 (BITS(i, 19, 12) << 11) | \
                                 (BITS(i, 20, 20) << 10) | \
                                  BITS(i, 30, 21)), 20) << 1; } while (0);

static int   inst_num = 1;
static char *inst_op = NULL;
static bool  inst_func_call = false;
static bool  inst_func_ret = false;
static char *inst_func_name_arr[1024];
static char **inst_func_name_head = inst_func_name_arr;
static int inst_func_call_depth = -1;

static void decode_operand(Decode *s,
                           int *rd,
                           int *rs1,
                           int *rs2,
                           word_t *src1,
                           word_t *src2,
                           word_t *imm,
                           int type) {
  uint32_t i = s->isa.inst.val;
  int rd_t = BITS(i, 11, 7);
  int rs1_t = BITS(i, 19, 15);
  int rs2_t = BITS(i, 24, 20);

  *rd = rd_t;
  *rs1 = rs1_t;
  *rs2 = rs2_t;

  switch (type) {
    case TYPE_R: src1R(); src2R();         break;
    case TYPE_I: src1R();          immI(); break;
    case TYPE_S: src1R(); src2R(); immS(); break;
    case TYPE_B: src1R(); src2R(); immB(); break;
    case TYPE_U:                   immU(); break;
    case TYPE_J:                   immJ(); break;
  }
#ifdef CONFIG_INST
  printf("rd:   %d\n", *rd);
  printf("rs1:  %d\n", *rs1);
  printf("rs2:  %d\n", *rs2);
  printf("src1: " PRINTF_BIN_PATTERN_INT64 "\n", PRINTF_BIN_INT64(*src1));
  printf("src2: " PRINTF_BIN_PATTERN_INT64 "\n", PRINTF_BIN_INT64(*src2));
  printf("imm:  " PRINTF_BIN_PATTERN_INT64 "\n", PRINTF_BIN_INT64(*imm));
#endif
}

static int decode_exec(Decode *s) {
  int rd = 0;
  int rs1 = 0;
  int rs2 = 0;
  word_t src1 = 0, src2 = 0, imm = 0;
  s->dnpc = s->snpc;

#define INSTPAT_INST(s) ((s)->isa.inst.val)
#define INSTPAT_MATCH(s, name, type, ... /* execute body */ ) { \
  inst_op = str(name); \
  decode_operand(s, &rd, &rs1, &rs2, &src1, &src2, &imm, concat(TYPE_, type)); \
  __VA_ARGS__ ; \
}

  INSTPAT_START();

#ifdef CONFIG_INST
  printf("num:  %d\n", inst_num);
  printf("inst: " PRINTF_BIN_PATTERN_INST "\n",
         PRINTF_BIN_INST(s->isa.inst.val));
  printf("pc:   " FMT_WORD "\n", s->pc);
  printf("dnpc: " FMT_WORD "\n", s->dnpc);
  printf("op:   %s\n", inst_op);
#endif
  inst_num++;

  INSTPAT("??????? ????? ????? ??? ????? 01101 11",
          lui,
          U,
          R(rd) = imm);
  INSTPAT("??????? ????? ????? ??? ????? 00101 11",
          auipc,
          U,
          R(rd) = s->pc + imm);
  INSTPAT("??????? ????? ????? ??? ????? 11011 11",
          jal,
          J,
          R(rd) = s->pc + 4; \
          s->dnpc = s->pc + imm; \
          inst_func_call = true);
  INSTPAT("??????? ????? ????? 000 ????? 11001 11",
          jalr,
          I,
          R(rd) = s->pc + 4; \
          s->dnpc = ((src1 + imm) & -1); \
          if (rd == 0 && rs1 == 1 && imm == 0) { \
            inst_func_ret = true;
          }
          else {
            inst_func_call = true;
          });
  INSTPAT("??????? ????? ????? 000 ????? 11000 11",
          beq,
          B,
          s->dnpc = (src1 == src2) ? (s->pc + imm) : s->dnpc);
  INSTPAT("??????? ????? ????? 001 ????? 11000 11",
          bne,
          B,
          s->dnpc = (src1 != src2) ? (s->pc + imm) : s->dnpc);
  INSTPAT("??????? ????? ????? 100 ????? 11000 11",
          blt,
          B,
          s->dnpc = ((sword_t)src1 < (sword_t)src2) ? \
                    (s->pc + imm) : s->dnpc);
  INSTPAT("??????? ????? ????? 101 ????? 11000 11",
          bge,
          B,
          s->dnpc = ((sword_t)src1 >= (sword_t)src2) ? \
                    (s->pc + imm) : s->dnpc);
  INSTPAT("??????? ????? ????? 110 ????? 11000 11",
          bltu,
          B,
          s->dnpc = (src1 < src2) ? (s->pc + imm) : s->dnpc);
  INSTPAT("??????? ????? ????? 111 ????? 11000 11",
          bgeu,
          B,
          s->dnpc = (src1 >= src2) ? (s->pc + imm) : s->dnpc);
  INSTPAT("??????? ????? ????? 000 ????? 00000 11",
          lb,
          I,
          R(rd) = SEXT(Mr(src1 + imm, 1), 8));
  INSTPAT("??????? ????? ????? 001 ????? 00000 11",
          lh,
          I,
          R(rd) = SEXT(Mr(src1 + imm, 2), 16));
  INSTPAT("??????? ????? ????? 010 ????? 00000 11",
          lw,
          I,
          R(rd) = SEXT(Mr(src1 + imm, 4), 32));
  INSTPAT("??????? ????? ????? 011 ????? 00000 11",
          ld,
          I,
          R(rd) = Mr(src1 + imm, 8));
  INSTPAT("??????? ????? ????? 100 ????? 00000 11",
          lbu,
          I,
          R(rd) = Mr(src1 + imm, 1));
  INSTPAT("??????? ????? ????? 101 ????? 00000 11",
          lhu,
          I,
          R(rd) = Mr(src1 + imm, 2));
  INSTPAT("??????? ????? ????? 000 ????? 01000 11",
          sb,
          S,
          Mw(src1 + imm, 1, src2));
  INSTPAT("??????? ????? ????? 001 ????? 01000 11",
          sh,
          S,
          Mw(src1 + imm, 2, src2));
  INSTPAT("??????? ????? ????? 010 ????? 01000 11",
          sw,
          S,
          Mw(src1 + imm, 4, src2));
  INSTPAT("??????? ????? ????? 011 ????? 01000 11",
          sd,
          S,
          Mw(src1 + imm, 8, src2));
  INSTPAT("??????? ????? ????? 000 ????? 00100 11",
          addi,
          I,
          R(rd) = src1 + imm);
  INSTPAT("??????? ????? ????? 011 ????? 00100 11",
          sltiu,
          I,
          R(rd) = (src1 < imm) ? 1 : 0);
  INSTPAT("??????? ????? ????? 100 ????? 00100 11",
          xori,
          I,
          R(rd) = src1 ^ imm);
  INSTPAT("??????? ????? ????? 111 ????? 00100 11",
          andi,
          I,
          R(rd) = src1 & imm);
  INSTPAT("000000 ?????? ????? 001 ????? 00100 11",
          slli,
          I,
          R(rd) = src1 << BITS(imm, 5, 0));
  INSTPAT("000000 ?????? ????? 101 ????? 00100 11",
          srli,
          I,
          R(rd) = src1 >> BITS(imm, 5, 0));
  INSTPAT("010000 ?????? ????? 101 ????? 00100 11",
          srai,
          I,
          imm = BITS(imm, 5, 0); \
          word_t bit_upper = BITS(src1, 63, 63); \
          word_t bits = src1; \
          for (int i = 0; i < imm; i++) { \
            bits = bits >> 1; \
            bits = (bit_upper << 63) | bits; \
          } \
          R(rd) = bits);
  INSTPAT("0000000 ????? ????? 000 ????? 01100 11",
          add,
          R,
          R(rd) = src1 + src2);
  INSTPAT("0000001 ????? ????? 000 ????? 01100 11",
          mul,
          R,
          R(rd) = src1 * src2);
  INSTPAT("0100000 ????? ????? 000 ????? 01100 11",
          sub,
          R,
          R(rd) = src1 - src2);
  INSTPAT("0000000 ????? ????? 010 ????? 01100 11",
          slt,
          R,
          R(rd) = ((sword_t)src1 < (sword_t)src2) ? 1 : 0);
  INSTPAT("0000000 ????? ????? 011 ????? 01100 11",
          sltu,
          R,
          R(rd) = (src1 < src2) ? 1 : 0);
  INSTPAT("0100000 ?????? ????? 101 ????? 01100 11",
          sra,
          R,
          imm = BITS(src2, 5, 0); \
          word_t bit_upper = BITS(src1, 63, 63); \
          word_t bits = src1; \
          for (int i = 0; i < imm; i++) { \
            bits = bits >> 1; \
            bits = (bit_upper << 63) | bits; \
          } \
          R(rd) = bits);
  INSTPAT("0000000 ????? ????? 110 ????? 01100 11",
          or,
          R,
          R(rd) = src1 | src2);
  INSTPAT("0000000 ????? ????? 111 ????? 01100 11",
          and,
          R,
          R(rd) = src1 & src2);
  INSTPAT("??????? ????? ????? 000 ????? 00110 11",
          addiw,
          I,
          R(rd) = SEXT(BITS(src1 + imm, 31, 0), 32));
  INSTPAT("0100000 ????? ????? 101 ????? 00110 11",
          sraiw,
          I,
          imm = BITS(imm, 5, 0); \
          word_t bit_upper = BITS(src1, 31, 31); \
          word_t bits = BITS(src1, 31, 0); \
          for (int i = 0; i < imm; i++) { \
            bits = bits >> 1; \
            bits = (bit_upper << 31) | bits; \
          } \
          R(rd) = SEXT(bits, 32));
  INSTPAT("000000 ?????? ????? 001 ????? 00110 11",
          slliw,
          I,
          R(rd) = SEXT(BITS(src1 << BITS(imm, 5, 0), 31, 0), 32));
  INSTPAT("000000 ?????? ????? 101 ????? 00110 11",
          srliw,
          I,
          R(rd) = SEXT(BITS(src1, 31, 0) >> BITS(imm, 5, 0), 32));
  INSTPAT("0000000 ????? ????? 000 ????? 01110 11",
          addw,
          R,
          R(rd) = SEXT(BITS(src1 + src2, 31, 0), 32));
  INSTPAT("0000001 ????? ????? 000 ????? 01110 11",
          mulw,
          R,
          R(rd) = SEXT(BITS(src1 * src2, 31, 0), 32));
  INSTPAT("0100000 ????? ????? 000 ????? 01110 11",
          subw,
          R,
          R(rd) = SEXT(BITS(src1 - src2, 31, 0), 32));
  INSTPAT("0000000 ????? ????? 001 ????? 01110 11",
          sllw,
          R,
          R(rd) = SEXT(BITS(BITS(src1, 31, 0) << \
                              BITS(src2, 4, 0), 31, 0), 32));
  INSTPAT("0000001 ????? ????? 100 ????? 01110 11",
          divw,
          R,
          R(rd) = SEXT(BITS(src1, 31, 0) / BITS(src2, 31, 0), 32));
  INSTPAT("0100000 ????? ????? 101 ????? 01110 11",
          sraw,
          R,
          imm = BITS(src2, 4, 0); \
          word_t bit_upper = BITS(src1, 31, 31); \
          word_t bits = BITS(src1, 31, 0); \
          for (int i = 0; i < imm; i++) { \
            bits = bits >> 1; \
            bits = (bit_upper << 31) | bits; \
          } \
          R(rd) = SEXT(bits, 32));
  INSTPAT("000000 ?????? ????? 101 ????? 01110 11",
          srlw,
          R,
          R(rd) = SEXT(BITS(src1, 31, 0) >> BITS(src2, 4, 0), 32));
  INSTPAT("0000001 ????? ????? 110 ????? 01110 11",
          remw,
          R,
          R(rd) = SEXT(BITS(src1, 31, 0) % BITS(src2, 31, 0), 32));
  INSTPAT("0000001 ????? ????? 111 ????? 01110 11",
          remuw,
          R,
          R(rd) = SEXT(BITS(src1, 31, 0) % BITS(src2, 31, 0), 32));
  INSTPAT("0000000 00001 00000 000 00000 11100 11",
          ebreak,
          N,
          NEMUTRAP(s->pc, R(10)));
  INSTPAT("??????? ????? ????? ??? ????? ????? ??",
          inv,
          N,
          INV(s->pc));
  INSTPAT_END();

  R(0) = 0;

#ifdef CONFIG_INST
#ifdef CONFIG_FTRACE_COND
  printf("dnpc: " FMT_WORD "\n", s->dnpc);
#else
  printf("dnpc: " FMT_WORD "\n\n", s->dnpc);
#endif
#endif

#ifdef CONFIG_FTRACE_COND
  if (inst_func_call || inst_func_ret) {
    printf("ftrace address: " "0x%08"PRIx32, (uint32_t)s->pc);
  }
#ifdef CONFIG_INST
  else {
    printf("\n");
  }
#endif

  if (inst_func_call) {
    inst_func_name_head++;
    inst_func_call_depth++;
    if (*inst_func_name_head == NULL) {
      *inst_func_name_head = (char *)malloc(sizeof(char *) * 256);
    }
    strcpy(*inst_func_name_head, elf_get_func(s->dnpc));

    char printf_format[256] = " call %*s[%s@" "0x%08"PRIx32 "]\n";
#ifdef CONFIG_INST
    strcat(printf_format, "\n");
#endif

    printf(printf_format, inst_func_call_depth * 2, "", *inst_func_name_head, (uint32_t)s->dnpc);
    inst_func_call = false;
  }

  if (inst_func_ret) {
    inst_func_name_head--;
    inst_func_call_depth--;

    char printf_format[256] = " ret  %*s[%s]\n";
#ifdef CONFIG_INST
    strcat(printf_format, "\n");
#endif

    printf(printf_format, inst_func_call_depth * 2, "", *inst_func_name_head);
    inst_func_ret = false;
  }
#endif

  return 0;
}

int isa_exec_once(Decode *s) {
  s->isa.inst.val = inst_fetch(&s->snpc, 4);
  return decode_exec(s);
}
