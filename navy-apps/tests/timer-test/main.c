#include <stdio.h>
#include <sys/time.h>

int main() {
    int sec = 1;
    struct timeval timer;
    timer.tv_sec  = 0;
    timer.tv_usec = 0;
    while (1) {
        while (timer.tv_sec < sec) {
            gettimeofday(&timer, NULL);
        }
        printf("timer: %d seconds\n", sec);
        sec++;
    }

    return 0;
}

