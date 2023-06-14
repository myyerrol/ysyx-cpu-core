#include <stdio.h>
#include <sys/time.h>
#include <NDL.h>

int main() {
    // uint32_t sec = 1;
    // struct timeval timer;
    // timer.tv_sec  = 0;
    // timer.tv_usec = 0;

    // while (1) {
    //     while (timer.tv_sec < sec) {
    //         gettimeofday(&timer, NULL);
    //     }
    //     printf("timer: %d seconds\n", sec);
    //     sec++;
    // }

    // return 0;

    uint32_t sec   = 1;
    uint32_t sec_t = 0;

    while (1) {
        while (sec_t < sec) {
            sec_t = NDL_GetTicks() / 1000;
        }
        printf("timer: %d seconds\n", sec);
        sec++;
    }

    return 0;
}
