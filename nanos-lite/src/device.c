#include <common.h>

#if defined(MULTIPROGRAM) && !defined(TIME_SHARING)
# define MULTIPROGRAM_YIELD() yield()
#else
# define MULTIPROGRAM_YIELD()
#endif

#define NAME(key) \
  [AM_KEY_##key] = #key,

static const char *keyname[256] __attribute__((used)) = {
  [AM_KEY_NONE] = "NONE",
  AM_KEYS(NAME)
};

size_t serial_write(const void *buf, size_t offset, size_t len) {
  size_t i = 0;
  for(; len > 0; len--) {
    putch(((char*)buf)[i]);
    i++;
  }
  return i;
}

size_t events_read(void *buf, size_t offset, size_t len) {
  AM_INPUT_KEYBRD_T kbd = io_read(AM_INPUT_KEYBRD);
  if (kbd.keycode != 0) {
    if (kbd.keydown) {
      snprintf((char *)buf, len, "kd %2d %s", kbd.keycode, keyname[kbd.keycode]);
    }
    else {
      snprintf((char *)buf, len, "ku %2d %s", kbd.keycode, keyname[kbd.keycode]);
    }
    return len;
  }
  else {
    return 0;
  }
}

size_t dispinfo_read(void *buf, size_t offset, size_t len) {
  AM_GPU_CONFIG_T cfg = io_read(AM_GPU_CONFIG);
  snprintf((char *)buf, len, "width: %d, height: %d", cfg.width, cfg.height);
  return 1;
}

size_t fb_write(const void *buf, size_t offset, size_t len) {
  AM_GPU_CONFIG_T cfg = io_read(AM_GPU_CONFIG);

  AM_GPU_FBDRAW_T ctl;
  ctl.pixels = (void *)buf;
  ctl.sync = true;

  ctl.x = offset % cfg.width;
  ctl.y = offset / cfg.width;
  ctl.w = len >> 32;
  ctl.h = len & 0x00000000FFFFFFFF;

  printf("ctl.x: %d, ctl.y: %d\n", ctl.x, ctl.y);

  io_write(AM_GPU_FBDRAW, ctl.x, ctl.y, ctl.pixels, ctl.w, ctl.h, ctl.sync);

  return 0;
}

void init_device() {
  Log("Initializing devices...");
  ioe_init();
}
