#include <utils/timer.h>

IFDEF(CONFIG_TIMER_CLOCK_GETTIME,
      static_assert(CLOCKS_PER_SEC == 1000000, "CLOCKS_PER_SEC != 1000000"));
IFDEF(CONFIG_TIMER_CLOCK_GETTIME,
      static_assert(sizeof(clock_t) == 8, "sizeof(clock_t) != 8"));

static uint64_t boot_time = 0;

static uint64_t getTimerInternal() {
#if defined(CONFIG_TIMER_GETTIMEOFDAY)
    struct timeval now;
    gettimeofday(&now, NULL);
    uint64_t us = now.tv_sec * 1000000 + now.tv_usec;
#else
    struct timespec now;
    clock_gettime(CLOCK_MONOTONIC_COARSE, &now);
    uint64_t us = now.tv_sec * 1000000 + now.tv_nsec / 1000;
#endif
    return us;
}

uint64_t getTimerValue() {
    if (boot_time == 0) boot_time = getTimerInternal();
    uint64_t curr_time = getTimerInternal();
    return curr_time - boot_time;
}
