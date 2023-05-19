#ifndef __KEYBOARD_H__
#define __KEYBOARD_H__

#include <common.h>

#define KEYDOWN_MASK 0x8000

#define KEYS(f) \
    f(ESCAPE) f(F1) f(F2) f(F3) f(F4) f(F5) f(F6) f(F7) f(F8) f(F9) f(F10) \
    f(F11) f(F12) f(GRAVE) f(1) f(2) f(3) f(4) f(5) f(6) f(7) f(8) f(9) f(0) \
    f(MINUS) f(EQUALS) f(BACKSPACE) f(TAB) f(Q) f(W) f(E) f(R) f(T) f(Y) f(U) \
    f(I) f(O) f(P) f(LEFTBRACKET) f(RIGHTBRACKET) f(BACKSLASH) f(CAPSLOCK) \
    f(A) f(S) f(D) f(F) f(G) f(H) f(J) f(K) f(L) f(SEMICOLON) f(APOSTROPHE) \
    f(RETURN) f(LSHIFT) f(Z) f(X) f(C) f(V) f(B) f(N) f(M) f(COMMA) f(PERIOD) \
    f(SLASH) f(RSHIFT) f(LCTRL) f(APPLICATION) f(LALT) f(SPACE) f(RALT) \
    f(RCTRL) f(UP) f(DOWN) f(LEFT) f(RIGHT) f(INSERT) f(DELETE) f(HOME) \
    f(END) f(PAGEUP) f(PAGEDOWN)

#define KEY_NAME(k) _KEY_##k,
#define SDL_KEYMAP(k) keymap[concat(SDL_SCANCODE_, k)] = concat(_KEY_, k);

enum {
    KEY_NONE = 0,
    MAP(KEYS, KEY_NAME)
};

uint32_t dequeueDiviceKey();
void initDeviceKey();
void sendDeviceKey(uint8_t scancode, bool is_keydown);

#endif
