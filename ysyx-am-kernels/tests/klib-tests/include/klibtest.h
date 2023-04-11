#ifndef __KLIBTEST_H__
#define __KLIBTEST_H__

#include <am.h>
#include <klib.h>
#include <klib-macros.h>

extern void (*entry)();

#define CASE(id, entry_, ...) \
    case id: { \
        void entry_(); \
        entry = entry_; \
        __VA_ARGS__; \
        entry_(); \
        break; \
    }

inline void check(bool cond) {
    if (!cond) halt(1);
}

#define N 32
uint8_t data[N];

inline void reset() {
    int i;
    for (i = 0; i < N; i++) {
        data[i] = i + 1;
    }
}

// 检查[l,r)区间中的值是否依次为val, val + 1, val + 2...
inline void check_seq(int l, int r, int val) {
    int i;
    for (i = l; i < r; i++) {
        check(data[i] == val + i - l);
    }
}

// 检查[l,r)区间中的值是否均为val
inline void check_eq(int l, int r, int val) {
    int i;
    for (i = l; i < r; i++) {
        check(data[i] == val);
    }
}


#endif
