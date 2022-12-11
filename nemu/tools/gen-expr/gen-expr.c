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

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <assert.h>
#include <string.h>

#define BUF_LENGTH     65536
#define DEBUG_GEN_EXPR 0

// this should be enough
static char buf[BUF_LENGTH] = {};
static char code_buf[BUF_LENGTH + 128] = {}; // a little larger than `buf`
static char *code_format =
"#include <stdio.h>\n "
"int main() { "
"  int a = -1; "
"  int b = %s; "
"  if (b > 0) { "
"    a = b; "
"  } "
"  printf(\"%%d\", a); "
"  return 0; "
"}";
// static char *code_format =
// "#include <stdio.h>\n"
// "int main() { "
// "  unsigned result = %s; "
// "  printf(\"%%u\", result); "
// "  return 0; "
// "}";

static uint32_t calc_expr(int *result_flag) {
  memset(code_buf, '\0', strlen(code_buf));
  sprintf(code_buf, code_format, buf);

  FILE *fp = fopen("/tmp/.code.c", "w");
  assert(fp != NULL);
  fputs(code_buf, fp);
  fclose(fp);

  fp = fopen("/tmp/.code_build.txt", "w");
  assert(fp != NULL);
  fclose(fp);

  int ret = system(
    "gcc /tmp/.code.c -Werror -o /tmp/.expr > /tmp/.code_build.txt 2>&1");
  if (ret != 0) {
    *result_flag = -1;
    return 0;
  }

  fp = popen("/tmp/.expr", "r");
  assert(fp != NULL);

  int result = 0;
  ret = fscanf(fp, "%d", &result);
  pclose(fp);
  if (result == -1) {
    *result_flag = -1;
    return 0;
  }

  return result;
}

static uint32_t choose(uint32_t n) {
  uint32_t num = rand() % n;
  return num;
}

static void gen_num() {
  uint32_t num = rand() % 100 + 1;
  sprintf(buf + strlen(buf), "%u", num);
#if DEBUG_GEN_EXPR
  printf("buf gen_num    : %s\n", buf);
#endif
}

static void gen(char str) {
  char arr[65536] = {};
  arr[0] = '\0';
  if (str == '(') {
    strcpy(arr, buf);
    memset(buf, '\0', strlen(buf));
    buf[0] = '(';
  }
  else if (str == ')') {
    arr[0] = ')';
  }
  strcat(buf, arr);
#if DEBUG_GEN_EXPR
  printf("buf gen        : %s\n", buf);
#endif
}

static void gen_rand_op() {
  uint32_t num = rand() % 4;
  char op = '+';
  switch (num) {
    case 0: {
      op = '+';
      break;
    }
    case 1: {
      op = '-';
      break;
    }
    case 2: {
      op = '*';
      break;
    }
    case 3: {
      op = '/';
      break;
    }
  }
  strcat(buf, &op);
#if DEBUG_GEN_EXPR
  printf("buf gen_rand_op: %s\n", buf);
#endif
}

static void gen_rand_expr() {
  switch (choose(3)) {
    case 0: {
      gen_num();
      break;
    }
    case 1: {
      gen('(');
      gen_rand_expr();
      gen(')');
      break;;
    }
    default: {
      gen_rand_expr();
      gen_rand_op();
      gen_rand_expr();
      break;
    }
  }
}

int main(int argc, char *argv[]) {
  int seed = time(0);
  srand(seed);

  int loop = 1;
  if (argc > 1) {
    sscanf(argv[1], "%d", &loop);
  }

  // 一直执行循环直到所有计算结果均正确
  for (int i = 0; i != loop;) {
    memset(buf, '\0', strlen(buf));
    gen_rand_expr();

    int result_flag = 0;
    uint32_t result = calc_expr(&result_flag);
    // 去除计算结果中无符号整数溢出的情况
    if (result_flag == -1) {
      continue;
    }
    i++;

#if DEBUG_GEN_EXPR
    printf("%u %s\n\n", result, buf);
#else
    printf("%u %s\n", result, buf);
#endif
  }
  return 0;
}
