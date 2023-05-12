#ifndef __EXPR_H__
#define __EXPR_H__

#include <common.h>

word_t handleSDBExpr(char *epxr, char *ret_ref, bool *success);
void initSDBExpr();
word_t testSDBExpr();

#endif
