#include <SDL2/SDL.h>

#include <utils/timer.h>
#include <device/device.h>
#include <device/keyboard.h>
#include <device/vga.h>
#include <state.h>

#define TIMER_HZ 60

void initDevice() {
    initDeviceKey();
    initDeviceVGA();
}

void updateDeviceState() {
    static uint64_t last = 0;
    uint64_t curr = getTimerValue();
    if (curr - last < 1000000 / TIMER_HZ) {
        return;
    }
    last = curr;

    IFDEF(CONFIG_HAS_VGA, updateDeviceVGAScreen());

    SDL_Event event;
    while (SDL_PollEvent(&event)) {
        switch (event.type) {
            case SDL_QUIT: {
                npc_state.state = NPC_QUIT;
                break;
            }
#ifdef CONFIG_HAS_KEYBOARD
            case SDL_KEYDOWN:
            case SDL_KEYUP: {
                uint8_t k = event.key.keysym.scancode;
                bool is_keydown = (event.key.type == SDL_KEYDOWN);
                sendDeviceKey(k, is_keydown);
                break;
            }
#endif
            default: break;
        }
    }
}
