#include <SDL2/SDL.h>

#include <device/keyboard.h>
#include <state.h>

static uint32_t keymap[256] = {};

static void initDeviceKeymap() {
    MAP(KEYS, SDL_KEYMAP)
}

#define KEY_QUEUE_LEN 1024
static int key_queue[KEY_QUEUE_LEN] = {};
static int key_f = 0, key_r = 0;

static void enqueueDeviceKey(uint32_t am_scancode) {
    key_queue[key_r] = am_scancode;
    key_r = (key_r + 1) % KEY_QUEUE_LEN;
    ASSERT(key_r != key_f, "[device] key queue overflow!");
}

uint32_t dequeueDiviceKey() {
    uint32_t key = KEY_NONE;
    if (key_f != key_r) {
        key = key_queue[key_f];
        key_f = (key_f + 1) % KEY_QUEUE_LEN;
    }
    return key;
}

void initDeviceKey() {
    initDeviceKeymap();
}

void sendDeviceKey(uint8_t scancode, bool is_keydown) {
    if (npc_state.state == NPC_RUNNING && keymap[scancode] != KEY_NONE) {
        uint32_t am_scancode = keymap[scancode] |
                              (is_keydown ? KEYDOWN_MASK : 0);
        enqueueDeviceKey(am_scancode);
    }
}
