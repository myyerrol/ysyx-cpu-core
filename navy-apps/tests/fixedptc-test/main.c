#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include "fixedptc.h"

void fixedpt_print(fixedpt A) {
    char num[20];
    fixedpt_str(A, num, -2);
    printf("%s",num);
}

void fixedpt_printn(fixedpt A) {
    fixedpt_print(A);
    printf("\n");
}

int main() {
    fixedpt A, B, C;

    A = fixedpt_rconst(2.5);
    B = fixedpt_fromint(4);
    C = fixedpt_add(A, B);
    printf("op: ");
    fixedpt_print(A);
    printf("+");
    fixedpt_print(B);
    printf("=");
    fixedpt_printn(C);

    A = fixedpt_rconst(3.81);
    B = fixedpt_rconst(21.4);
    C = fixedpt_muli(A, 21.4);
    printf("op: ");
    fixedpt_print(A);
    printf("*");
    fixedpt_print(B);
    printf("=");
    fixedpt_printn(C);

    A = fixedpt_rconst(1.975);
    B = fixedpt_rconst(4.024);
    C = fixedpt_divi(A, 4.024);
    printf("op: ");
    fixedpt_print(A);
    printf("/");
    fixedpt_print(B);
    printf("=");
    fixedpt_printn(C);

    printf("op: exp(1)=");
    fixedpt_printn(fixedpt_exp(FIXEDPT_ONE));

    printf("op: sqrt(25)=");
    fixedpt_printn(fixedpt_sqrt(fixedpt_rconst(25)));

    printf("op: sin(pi/2)=");
    fixedpt_printn(fixedpt_sin(FIXEDPT_HALF_PI));

    printf("op: 4^2.5=");
    fixedpt_printn(fixedpt_pow(fixedpt_rconst(4), fixedpt_rconst(2.5)));

    puts("");

    printf("func: ceil(0.505)=");
    fixedpt_printn(fixedpt_ceil(fixedpt_rconst(0.505)));

    printf("func: ceil(-0.505)=");
    fixedpt_printn(fixedpt_ceil(fixedpt_rconst(-0.505)));

    printf("func: floor(0.505)=");
    fixedpt_printn(fixedpt_floor(fixedpt_rconst(0.505)));

    printf("func: floor(-0.505)=");
    fixedpt_printn(fixedpt_floor(fixedpt_rconst(-0.505)));

    printf("func: floor(-NaN)=");
    fixedpt_printn(fixedpt_floor(0x80000001));

    printf("func: floor(+NaN)= ");
    fixedpt_printn(fixedpt_floor(0x7fffffff));

    printf("func: floor(-0)=");
    fixedpt_printn(fixedpt_floor(fixedpt_rconst(-0)));

    printf("func: floor(+0)=");
    fixedpt_printn(fixedpt_floor(fixedpt_rconst(+0)));

    printf("func: floor(-5)=");
    fixedpt_printn(fixedpt_floor(fixedpt_rconst(-5)));

    printf("func: floor(+5)= ");
    fixedpt_printn(fixedpt_floor(fixedpt_rconst(+5)));

    printf("func: ceil(-NaN)=");
    fixedpt_printn(fixedpt_ceil(0x80000001));

    printf("func: ceil(+NaN)= ");
    fixedpt_printn(fixedpt_ceil(0x7fffffff));

    printf("func: ceil(-0)=");
    fixedpt_printn(fixedpt_ceil(fixedpt_rconst(-0)));

    printf("func: ceil(+0)=");
    fixedpt_printn(fixedpt_ceil(fixedpt_rconst(+0)));

    printf("func: ceil(-5)=");
    fixedpt_printn(fixedpt_ceil(fixedpt_rconst(-5)));

    printf("func: ceil(+5)= ");
    fixedpt_printn(fixedpt_ceil(fixedpt_rconst(+5)));

    printf("func: abs(-505.50505)=");
    fixedpt_printn(fixedpt_abs(fixedpt_rconst(-505.50505)));

    return (0);
}
