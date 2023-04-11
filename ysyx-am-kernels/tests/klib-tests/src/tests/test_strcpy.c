#include <klibtest.h>

void test_strcpy() {
    int l, r;
    for (l = 0; l < N; l++) {
        for (r = l + 1; r <= N; r++) {
            reset();
            uint8_t val = (l + r) / 2;
            // strcpy(data + l, val, r - l);
            check_seq(0, l, 1);
            check_eq(l, r, val);
            // check_seq(r, N, r + 1);
        }
    }
}
