#ifndef __WATCH_H__
#define __WATCH_H__

#include <common.h>

void freeSDBWatch(int no);
void initSDBWatch();
void newSDBWatch(char *expr);
void printfSDBWatch();
void testSDBWatch();
int traceSDBWatch();

#endif