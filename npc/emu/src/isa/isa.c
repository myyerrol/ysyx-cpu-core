#include <isa/isa.h>
#include <memory/memory.h>

void initISA() {
    const uint32_t img[] = {
        0x00500093, // addi r1 r0 5
        0x00A08113, // addi r2 r1 10
        // 0x00013183, // ld r3 0(r2)
        0x00100073  // ebreak
    };
    memcpy(convertGuestToHost(RESET_VECTOR), img, sizeof(img));

    cpu.pc = RESET_VECTOR;
    cpu.gpr[0] = 0;
}
