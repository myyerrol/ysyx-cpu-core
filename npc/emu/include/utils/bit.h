#ifndef __BIT_H__
#define __BIT_H__

#include <common.h>

#define PRINTF_BIN_PATTERN_INT2 "%c%c"
#define PRINTF_BIN_PATTERN_INT3 "%c%c%c "
#define PRINTF_BIN_PATTERN_INT5 "%c%c%c%c%c "
#define PRINTF_BIN_PATTERN_INT7 "%c%c%c%c%c%c%c "
#define PRINTF_BIN_PATTERN_INT8 "%c%c%c%c%c%c%c%c "
#define PRINTF_BIN_PATTERN_INT16 \
    PRINTF_BIN_PATTERN_INT8 PRINTF_BIN_PATTERN_INT8
#define PRINTF_BIN_PATTERN_INT32 \
    PRINTF_BIN_PATTERN_INT16 PRINTF_BIN_PATTERN_INT16
#define PRINTF_BIN_PATTERN_INT64 \
    PRINTF_BIN_PATTERN_INT32 PRINTF_BIN_PATTERN_INT32
#define PRINTF_BIN_PATTERN_INST  \
    PRINTF_BIN_PATTERN_INT7 PRINTF_BIN_PATTERN_INT5 PRINTF_BIN_PATTERN_INT5 \
    PRINTF_BIN_PATTERN_INT3 PRINTF_BIN_PATTERN_INT5 PRINTF_BIN_PATTERN_INT5 \
    PRINTF_BIN_PATTERN_INT2
#define PRINTF_BIN_INT2(i)        \
    (((i) & 0x02ll) ? '1' : '0'), \
    (((i) & 0x01ll) ? '1' : '0')
#define PRINTF_BIN_INT3(i)        \
    (((i) & 0x04ll) ? '1' : '0'), \
    (((i) & 0x02ll) ? '1' : '0'), \
    (((i) & 0x01ll) ? '1' : '0')
#define PRINTF_BIN_INT5(i)        \
    (((i) & 0x10ll) ? '1' : '0'), \
    (((i) & 0x08ll) ? '1' : '0'), \
    (((i) & 0x04ll) ? '1' : '0'), \
    (((i) & 0x02ll) ? '1' : '0'), \
    (((i) & 0x01ll) ? '1' : '0')
#define PRINTF_BIN_INT7(i)        \
    (((i) & 0x40ll) ? '1' : '0'), \
    (((i) & 0x20ll) ? '1' : '0'), \
    (((i) & 0x10ll) ? '1' : '0'), \
    (((i) & 0x08ll) ? '1' : '0'), \
    (((i) & 0x04ll) ? '1' : '0'), \
    (((i) & 0x02ll) ? '1' : '0'), \
    (((i) & 0x01ll) ? '1' : '0')
#define PRINTF_BIN_INT8(i)        \
    (((i) & 0x80ll) ? '1' : '0'), \
    (((i) & 0x40ll) ? '1' : '0'), \
    (((i) & 0x20ll) ? '1' : '0'), \
    (((i) & 0x10ll) ? '1' : '0'), \
    (((i) & 0x08ll) ? '1' : '0'), \
    (((i) & 0x04ll) ? '1' : '0'), \
    (((i) & 0x02ll) ? '1' : '0'), \
    (((i) & 0x01ll) ? '1' : '0')
#define PRINTF_BIN_INT16(i) \
    PRINTF_BIN_INT8((i) >> 8), PRINTF_BIN_INT8(i)
#define PRINTF_BIN_INT32(i) \
    PRINTF_BIN_INT16((i) >> 16), PRINTF_BIN_INT16(i)
#define PRINTF_BIN_INT64(i) \
    PRINTF_BIN_INT32((i) >> 32), PRINTF_BIN_INT32(i)
#define PRINTF_BIN_INST(i)  \
    PRINTF_BIN_INT7((i) >> 25), PRINTF_BIN_INT5((i) >> 20), \
    PRINTF_BIN_INT5((i) >> 15), PRINTF_BIN_INT3((i) >> 12), \
    PRINTF_BIN_INT5((i) >> 7),  PRINTF_BIN_INT5((i) >> 2),  \
    PRINTF_BIN_INT2(i)

#endif
