#include <am.h>
#include <nemu.h>

#define SYNC_ADDR (VGACTL_ADDR + 4)
#define CONFIG_GPU_TEST 0

static uint32_t width  = 0;
static uint32_t height = 0;

void __am_gpu_init() {
  uint32_t gpu_cfg = inl(VGACTL_ADDR);
  width  = (gpu_cfg & 0xffff0000) >> 16;
  height = (gpu_cfg & 0x0000ffff);
#if CONFIG_GPU_TEST
  int i;
  int w = width;
  int h = height;
  uint32_t *fb = (uint32_t *)(uintptr_t)FB_ADDR;
  for (i = 0; i < w * h; i++) fb[i] = i;
  outl(SYNC_ADDR, 1);
#endif
}

void __am_gpu_config(AM_GPU_CONFIG_T *cfg) {
  *cfg = (AM_GPU_CONFIG_T) {
    .present = true, .has_accel = false,
    .width = width, .height = height,
    .vmemsz = 0
  };
}

void __am_gpu_fbdraw(AM_GPU_FBDRAW_T *ctl) {
  int x = ctl->x;
  int y = ctl->y;
  int w = ctl->w;
  int h = ctl->h;
  uint32_t *pixels = (uint32_t *)(ctl->pixels);
  uint32_t *fb = (uint32_t *)(uintptr_t)FB_ADDR;

  int count_x = 0;
  int count_y = 0;
  int index   = 0;
  for (int i = 0; i < w * h; i++) {
    if (count_x > 0 && count_x < 12) {
      index++;
    }
    else {
      if (count_y < 9) {
        index = width * (y + count_y) + x;
        count_y++;
      }
      else {
        break;
      }
    }
    count_x++;

    fb[index] = pixels[i];
  }

#if !CONFIG_GPU_TEST
  if (ctl->sync) {
    outl(SYNC_ADDR, 1);
  }
#endif
}

void __am_gpu_status(AM_GPU_STATUS_T *status) {
  status->ready = true;
}
