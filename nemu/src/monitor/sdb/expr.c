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

#include <isa.h>

/* We use the POSIX regex functions to process regular expressions.
 * Type 'man regex' for more information about POSIX regex functions.
 */
#include <regex.h>

enum {
  TK_NOTYPE = 256, TK_EQ,

  /* TODO: Add more token types */
  TK_INTEGER
};

static struct rule {
  const char *regex;
  int token_type;
} rules[] = {

  /* TODO: Add more rules.
   * Pay attention to the precedence level of different rules.
   */

  {" +", TK_NOTYPE},    // spaces
  {"\\+", '+'},         // plus
  {"==", TK_EQ},        // equal

  {"[0-9]+", TK_INTEGER},
  {"\\-", '-'},
  {"\\*", '*'},
  {"\\/", '/'},
  {"\\(", '('},
  {"\\)", ')'}
};

#define NR_REGEX ARRLEN(rules)

#define DEBUG_EXPR_MAKE_TOKEN 0
#define DEBUG_EXPR_EVAL 1

static regex_t re[NR_REGEX] = {};

/* Rules are used for many times.
 * Therefore we compile them only once before any usage.
 */
void init_regex() {
  int i;
  char error_msg[128];
  int ret;

  for (i = 0; i < NR_REGEX; i ++) {
    ret = regcomp(&re[i], rules[i].regex, REG_EXTENDED);
    if (ret != 0) {
      regerror(ret, &re[i], error_msg, 128);
      panic("regex compilation failed: %s\n%s", error_msg, rules[i].regex);
    }
  }
}

typedef struct token {
  int type;
  char str[32];
} Token;

static Token tokens[32] __attribute__((used)) = {};
static int nr_token __attribute__((used))  = 0;

static bool make_token(char *e) {
  int position = 0;
  int i;
  regmatch_t pmatch;

  nr_token = 0;

  while (e[position] != '\0') {
    /* Try all rules one by one. */
    for (i = 0; i < NR_REGEX; i ++) {
      if (regexec(&re[i], e + position, 1, &pmatch, 0) == 0 && pmatch.rm_so == 0) {
        char *substr_start = e + position;
        int substr_len = pmatch.rm_eo;

        Log("match rules[%d] = \"%s\" at position %d with len %d: %.*s",
            i, rules[i].regex, position, substr_len, substr_len, substr_start);

        position += substr_len;

        /* TODO: Now a new token is recognized with rules[i]. Add codes
         * to record the token in the array `tokens'. For certain types
         * of tokens, some extra actions should be performed.
         */

        // switch (rules[i].token_type) {
        //   default: TODO();
        // }

#if DEBUG_EXPR_MAKE_TOKEN
        Log("substr_start: %s", substr_start);
#endif

        Token token = { 0, "" };
        token.type = rules[i].token_type;
#if DEBUG_EXPR_MAKE_TOKEN
        Log("token.type: %d", token.type);
#endif

        if (substr_len > 32) {
          substr_len = 32;
        }
        strncpy(token.str, substr_start, substr_len);
#if DEBUG_EXPR_MAKE_TOKEN
        Log("token.str: %s\n", token.str);
#endif

        if (nr_token < ARRLEN(tokens)) {
          tokens[nr_token] = token;
        }
        nr_token++;

        break;
      }
    }

    if (i == NR_REGEX) {
      printf("no match at position %d\n%s\n%*.s^\n", position, e, position, "");
      return false;
    }
  }

  return true;
}

bool check_parentheses(word_t p, word_t q) {
  char arr[100] = "\0";

  for (int i = p, j = 0; i <= q; i++) {
    Token token = tokens[i];
    int type = token.type;
    // 表达式第一个元素必须是左括号
    if (i == p && type != '(') {
      return false;
    }
    if (type == '(') {
      arr[j] = '(';
      j++;
    }
    else if (type == ')') {
      j--;
      // 表达式第一个括号必须是左括号
      if (j < 0) {
        return false;
      }
      arr[j] = '0';
      // 表达式第一个左括号必须正好与最后一个右括号匹配
      if (arr[0] == '0' && i != q) {
        return false;
      }
    }
  }

  if (arr[0] == '0') {
    return true;
  }
  else {
    return false;
  }
}

static word_t find_op(word_t p, word_t q) {
  bool flag = false;
  int type_temp  = 0;
  int type_index = 0;

  for (int i = p; i <= q; i++) {
    Token token = tokens[i];
    int type = token.type;
    // 主运算符必须是运算符
    if (type == TK_INTEGER) {
      continue;
    }
    else if (type == '(') {
      flag = true;
    }
    else if (type == ')') {
      flag = false;
    }
    // 主运算符不会出现在括号里
    if (flag) {
      continue;
    }
    else {
      if (type_temp == 0) {
        type_temp = type;
        type_index = i;
      }
      else {
        // 主运算符优先级最低，同级别运算符以最后被结合的为准
        if (type_temp == '+' || type_temp == '-') {
          if (type == '+' || type == '-') {
            type_temp = type;
            type_index = i;
          }
        }
        else if (type_temp == '*' || type_temp == '/') {
          if (type == '+' || type == '-' || type == '*' || type == '/') {
            type_temp = type;
            type_index = i;
          }
        }
      }
    }
  }

  return type_index;
}

static word_t eval(word_t p, word_t q) {
  if (p > q) {
    return 0;
  }
  else if (p == q) {
    return strtol(tokens[p].str, NULL, 10);
  }
  else if (check_parentheses(p, q) == true) {
    return eval(p + 1, q - 1);
  }
  else {
    word_t op = find_op(p, q);
    word_t val1 = eval(p, op - 1);
    word_t val2 = eval(op + 1, q);

#if DEBUG_EXPR_EVAL
    Log("op: %s", tokens[op].str);
    Log("val1: %lu", val1);
    Log("val2: %lu", val2);
#endif

    word_t ret = 0;
    switch (tokens[op].type) {
      case '+': {
        ret = val1 + val2;
        break;
      }
      case '-': {
        ret = val1 - val2;
        break;
      }
      case '*': {
        ret = val1 * val2;
        break;
      }
      case '/': {
        ret = val1 / val2;
        break;
      }
      default: assert(0);
    }

#if DEBUG_EXPR_EVAL
    Log("ret: %lu\n", ret);
#endif

    return ret;
  }
}

word_t expr(char *e, bool *success) {
  if (!make_token(e)) {
    *success = false;
    return 0;
  }

  /* TODO: Insert codes to evaluate the expression. */
  return eval(0, nr_token - 1);
}
