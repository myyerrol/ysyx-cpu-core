#include <regex.h>

#include <memory/memory.h>
#include <monitor/sdb/expr.h>
#include <utils/str.h>

#include <isa/gpr.h>

enum {
    TK_NOTYPE = 256,
    TK_NUM_HEX,
    TK_NUM_DEC,
    TK_REG,
    TK_EQ,
    TK_EQN,
    TK_AND,
    TK_PTR_DEREF
};

static struct rule {
    const char *regex;
    int type;
    int prior;
} rules[] = {
    { " +", TK_NOTYPE -1 },
    { "^(0x)[0-9a-fA-F]+", TK_NUM_HEX, -1 },
    { "[0-9]+", TK_NUM_DEC, -1 },
    { "^(\\$)[\\$a-z][a-z0-9]", TK_REG, -1 },
    { "\\(", '(', -1 },
    { "\\)", ')', -1 },
    { "\\+", '+', 1 },
    { "\\-", '-', 1 },
    { "\\*", '*', 0 },
    { "\\/", '/', 0 },
    { "==", TK_EQ, 2 },
    { "!=", TK_EQN, 2 },
    { "&&", TK_AND, 3 }
};

#define NR_REGEX ARRLEN(rules)

#define TOKEN_ARR_LENGTH 65536
#define TOKEN_STR_LENGTH 256

static regex_t regex_arr[NR_REGEX] = {};

typedef struct token {
    int type;
    int prior;
    char str[TOKEN_STR_LENGTH];
} Token;

static Token tokens[TOKEN_ARR_LENGTH] __attribute__((used)) = {};
static int nr_token __attribute__((used)) = 0;

static bool handleSDBExprToken(char *e) {
    int position = 0;
    int i;
    regmatch_t pmatch;

    nr_token = 0;

    while (e[position] != '\0') {
        for (i = 0; i < NR_REGEX; i ++) {
            if (regexec(&regex_arr[i], e + position, 1, &pmatch, 0) == 0 &&
                pmatch.rm_so == 0) {
                char *substr_start = e + position;
                int substr_len = pmatch.rm_eo;

#ifdef CONFIG_SDB_EXPR_TOKEN
                LOG_BRIEF("[sdb] [expr] [token] match rule:  \"%s\"",
                          rules[i].regex);
                LOG_BRIEF("[sdb] [expr] [token] match pos:   %d", position);
                LOG_BRIEF("[sdb] [expr] [token] match len:   %d", substr_len);
                LOG_BRIEF("[sdb] [expr] [token] match str:   %.*s",
                          substr_len,
                          substr_start);
#endif
                position += substr_len;

#ifdef CONFIG_SDB_EXPR_TOKEN
                LOG_BRIEF("[sdb] [expr] [token] substr:      %s", substr_start);
#endif
                Token token = { 0, -1, "" };
                token.type = rules[i].type;
                token.prior = rules[i].prior;
#ifdef CONFIG_SDB_EXPR_TOKEN
                LOG_BRIEF("[sdb] [expr] [token] token.type:  %d", token.type);
                LOG_BRIEF("[sdb] [expr] [token] token.prior: %d", token.prior);
#endif
                if (substr_len > TOKEN_STR_LENGTH) {
                    substr_len = TOKEN_STR_LENGTH;
                }
                strncpy(token.str, substr_start, substr_len);
#ifdef CONFIG_SDB_EXPR_TOKEN
                LOG_BRIEF("[sdb] [expr] [token] token.str:   %s\n", token.str);
#endif
                if (nr_token < ARRLEN(tokens)) {
                    tokens[nr_token] = token;
                }
                nr_token++;
                break;

                switch (rules[i].type) {
                }
            }
        }

        if (i == NR_REGEX) {
            LOG_BRIEF("[sdb] [expr] [token] no match at position %d\n%s\n%*.s^",
                      position,
                      e,
                      position,
                      "");
            return false;
        }
    }

    return true;
}

static bool handleSDBExprBracket(word_t index_l, word_t index_r) {
    char arr[100] = "\0";

    for (int i = index_l, j = 0; i <= index_r; i++) {
        Token token = tokens[i];
        int type = token.type;
        // 表达式第一个元素必须是左括号
        if (i == index_l && type != '(') {
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
            if (arr[0] == '0' && i != index_r) {
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

static word_t handleSDBExprOp(word_t index_l, word_t index_r) {
    int type = 0;
    int prior = 0;
    int op_index = 0;
    int bracket_count = 0;
    bool bracket_flag = false;

    for (int i = index_l; i <= index_r; i++) {
        Token token = tokens[i];
        int type_curr = token.type;
        int prior_curr = token.prior;
        if (i == index_l && type_curr == TK_PTR_DEREF) {
            return i;
        }

        // 主运算符必须是运算符、关系符或逻辑符
        if (type_curr == TK_NOTYPE ||
            type_curr == TK_NUM_HEX ||
            type_curr == TK_NUM_DEC ||
            type_curr == TK_REG ||
            type_curr == TK_PTR_DEREF) {
            continue;
        }
        else if (type_curr == '(') {
            bracket_count++;
            bracket_flag = true;
        }
        else if (type_curr == ')') {
            bracket_count--;
            if (bracket_count == 0) {
                bracket_flag = false;
                continue;
            }
        }

        // 主运算符不会出现在括号里
        if (bracket_flag) {
            continue;
        }
        else {
            if (type == 0) {
                type = type_curr;
                prior = prior_curr;
                op_index = i;
            }
            else {
                // 主运算符优先级最低，同级别运算符以最后被结合的为准
                if (prior <= prior_curr) {
                    type = type_curr;
                    prior = prior_curr;
                    op_index = i;
                }
            }
        }
    }

    return op_index;
}

static word_t handleSDBExprEval(word_t index_l, word_t index_r) {
    if (index_l > index_r) {
        return 0;
    }
    else if (index_l == index_r) {
        char *token_str = tokens[index_l].str;
        if (token_str != NULL) {
            switch (tokens[index_l].type) {
                case TK_NUM_HEX: {
                    strrpc(token_str, (char *)"0x", (char *)"");
                    return strtoul(token_str, NULL, 16);
                }
                case TK_NUM_DEC: {
                    return strtoul(token_str, NULL, 10);
                }
                case TK_REG: {
                    bool success = false;
                    token_str = token_str + 1;
                    word_t val = getISAGPRData(token_str, &success);
                    if (success) {
                        return val;
                    }
                    else {
                        assert(0);
                    }
                }
                default: {
                    assert(0);
                }
            }
        }
        assert(0);
    }
    else if (handleSDBExprBracket(index_l, index_r) == true) {
        return handleSDBExprEval(index_l + 1, index_r - 1);
    }
    else {
        word_t op = handleSDBExprOp(index_l, index_r);
        word_t val1 = 0;
        // 指针解引用只有右侧子表达式
        int token_type = tokens[op].type;
        if (token_type != TK_PTR_DEREF) {
            val1 = handleSDBExprEval(index_l, op - 1);
        }
        word_t val2 = handleSDBExprEval(op + 1, index_r);

#ifdef CONFIG_SDB_EXPR_EVAL
        LOG_BRIEF("[sdb] [expr] [eval] op:   %s", tokens[op].str);
        LOG_BRIEF("[sdb] [expr] [eval] val1: %lu", val1);
        LOG_BRIEF("[sdb] [expr] [eval] val2: %lu", val2);
#endif

        word_t ret = 0;
        switch (token_type) {
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
            case TK_EQ: {
                if (val1 == val2) {
                    ret = 1;
                }
                else {
                    ret = 0;
                }
                break;
            }
            case TK_EQN: {
                if (val1 != val2) {
                    ret = 1;
                }
                else {
                    ret = 0;
                }
                break;
            }
            case TK_AND: {
                if (val1 && val2) {
                    ret = 1;
                }
                else {
                    ret = 0;
                }
                break;
            }
            case TK_PTR_DEREF: {
                if (judgeAddrIsInPhyMem(val2)) {
                    ret = readPhyMemData(val2, 8);
                }
                break;
            }
            default: {
                assert(0);
            }
        }

#ifdef CONFIG_SDB_EXPR_EVAL
        LOG_BRIEF("[sdb] [expr] [eval] ret:  %lu\n", ret);
#endif

        return ret;
    }
}

word_t handleSDBExpr(char *epxr, char *ret_ref, bool *success) {
    if (!handleSDBExprToken(epxr)) {
        *success = false;
        return 0;
    }

    for (int i = 0; i < nr_token; i++) {
        if (tokens[i].type == '*' &&
            (i == 0 ||
            tokens[i - 1].type == '+' ||
            tokens[i - 1].type == '-' ||
            tokens[i - 1].type == '-' ||
            tokens[i - 1].type == '*' ||
            tokens[i - 1].type == TK_EQ ||
            tokens[i - 1].type == TK_EQN ||
            tokens[i - 1].type == TK_AND)) {
            tokens[i].type = TK_PTR_DEREF;
        }
    }

    word_t ret = handleSDBExprEval(0, nr_token - 1);
    if (ret_ref != NULL) {
        *success = (ret == strtoul(ret_ref, NULL, 10)) ? true : false;
    }
    else {
        *success = true;
    }

#ifdef CONFIG_SDB_EXPR
    LOG_BRIEF("[sdb] [expr] success: %d, ret: %lu\n", *success, ret);
#endif

  return ret;
}

void initSDBExpr() {
    char error_msg[128];
    int ret;

    for (int i = 0; i < NR_REGEX; i ++) {
        ret = regcomp(&regex_arr[i], rules[i].regex, REG_EXTENDED);
        if (ret != 0) {
            regerror(ret, &regex_arr[i], error_msg, 128);
            PANIC("[sdb] [expr] regex compilation failed: %s\n%s", error_msg,
                                                                   rules[i].regex);
        }
    }
}

word_t testSDBExpr() {
    char str[TOKEN_ARR_LENGTH + 1];
    FILE *fp = fopen("./tools/gen-expr/input.txt", "r");
    assert(fp != NULL);
    while (fgets(str, TOKEN_ARR_LENGTH, fp) != NULL) {
        char *input_ret = strtok(str, " ");
        char *input_expr = strrpc(strtok(NULL, " "), (char *)"\n", (char *)"");
        bool success = false;
        handleSDBExpr(input_expr, input_ret, &success);
        memset(str, '\0', strlen(str));
    }
    fclose(fp);
    return 0;
}
