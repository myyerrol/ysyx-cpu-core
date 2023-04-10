#include <klibtest.h>

void (*entry)() = NULL;

static const char *tests[256] = {
    ['1'] = "test_memset"
};

int main(const char *args) {
    switch (args[0]) {
        CASE('1', test_memset);
        default: {
            printf("Usage: make run mainargs=*\n");
            for (int ch = 0; ch < 256; ch++) {
                if (tests[ch]) {
                    printf("  %c: %s\n", ch, tests[ch]);
                }
            }
        }
    }
}
