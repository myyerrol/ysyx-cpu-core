#include <NDL.h>
#include <SDL.h>
#include "assert.h"
#include <stdio.h>
#include <string.h>

#define keyname(k) #k,
#define ARRLEN(arr) (int)(sizeof(arr) / sizeof(arr[0]))

static const char *keyname[] = {
  "NONE",
  _KEYS(keyname)
};

uint8_t keystate[ARRLEN(keyname)];

int SDL_PushEvent(SDL_Event *ev) {
  return 0;
}

int SDL_PollEvent(SDL_Event *ev) {
  char buf[100];

  if (NDL_PollEvent(buf, ARRLEN(buf) - 1) != 0) {
    char *kbd_keydown = "kd";
    int   kbd_keycode = 0;
    char *kbd_keyname = "";

    sscanf(buf, "%s %2d %s", kbd_keydown, &kbd_keycode, kbd_keyname);
    printf("%s %2d %s\n", kbd_keydown, kbd_keycode, kbd_keyname);

    if (strcmp(kbd_keydown, "kd") == 0) {
      ev->key.type = SDL_KEYDOWN;
    }
    else if (strcmp (kbd_keydown, "ku ") == 0) {
      ev->key.type = SDL_KEYUP;
    }
    if (ev->type == SDL_KEYDOWN || ev->type == SDL_KEYUP) {
      if (kbd_keycode != 0) {
        keystate[kbd_keycode] = (ev->type == SDL_KEYDOWN) ? 1 : 0;
        ev->key.keysym.sym = kbd_keycode;
        return 1;
      }
    }
  }

  return 0;
}

int SDL_WaitEvent(SDL_Event *event) {
  while (SDL_PollEvent(event) == 0);
  return 1;
}

int SDL_PeepEvents(SDL_Event *ev, int numevents, int action, uint32_t mask) {
  return 0;
}

uint8_t* SDL_GetKeyState(int *numkeys) {
  if(numkeys != NULL) {
    *numkeys = ARRLEN(keyname);
  }
  return keystate;
}
