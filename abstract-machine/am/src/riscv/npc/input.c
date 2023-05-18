#include <am.h>

#include ISA_H

#define KBD_ADDR 0xa0000060
#define KEYDOWN_MASK 0x8000

void __am_input_keybrd(AM_INPUT_KEYBRD_T *kbd) {
  uint32_t key = inl(KBD_ADDR);
  kbd->keydown = key &  KEYDOWN_MASK;
  kbd->keycode = key & ~KEYDOWN_MASK;
}
