#include <stdint.h>
#include <stdio.h>

#define NREG 4
#define NMEM 16

// 定义指令格式
typedef union {
    struct { uint8_t rs : 2, rt : 2, op : 4; } rtype;
    struct { uint8_t addr : 4      , op : 4; } mtype;
    uint8_t inst;
} inst_t;

#define DECODE_R(inst) uint8_t rt = (inst).rtype.rt, rs = (inst).rtype.rs
#define DECODE_M(inst) uint8_t addr = (inst).mtype.addr

uint8_t pc = 0;       // PC, C语言中没有4位的数据类型, 我们采用8位类型来表示
uint8_t R[NREG] = {}; // 寄存器
uint8_t M[NMEM] = {   // 内存, 其中包含一个计算z = x + y的程序
    0b11100110,       // load  6#     | R[0] <- M[y]
    0b00000100,       // mov   r1, r0 | R[1] <- R[0]
    0b11100101,       // load  5#     | R[0] <- M[x]
    0b00010001,       // add   r0, r1 | R[0] <- R[0] + R[1]
    0b11110111,       // store 7#     | M[z] <- R[0]
    0b00010000,       // x = 16
    0b00100001,       // y = 33
    0b00000000,       // z = 0
};

int halt = 0; // 结束标志

// 执行一条指令
void exec_once() {
    inst_t this;
    this.inst = M[pc]; // 取指
    switch (this.rtype.op) {
        //  操作码译码       操作数译码           执行
        case 0b0000: { DECODE_R(this); R[rt]   = R[rs];   break; }
        case 0b0001: { DECODE_R(this); R[rt]  += R[rs];   break; }
        case 0b1110: { DECODE_M(this); R[0]    = M[addr]; break; }
        case 0b1111: { DECODE_M(this); M[addr] = R[0];    break; }
        default: {
            printf("Invalid instruction with opcode = %x, halting...\n",
                   this.rtype.op);
            halt = 1;
            break;
        }
    }
    pc ++; // 更新PC
}

int main() {
    while (1) {
        exec_once();
        if (halt) break;
    }
    printf("The result of 16 + 33 is %d\n", M[7]);
    return 0;
}
