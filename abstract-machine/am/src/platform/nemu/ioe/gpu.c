#include <am.h>
#include <nemu.h>
#include <klib.h>

#define SYNC_ADDR (VGACTL_ADDR + 4)

static uint32_t width  = 0;
static uint32_t height = 0;

void __am_gpu_init() {
  uint32_t gpu_cfg = inl(VGACTL_ADDR);
  width  = (gpu_cfg & 0xffff0000) >> 16;
  height = (gpu_cfg & 0x0000ffff);

  int i;
  int w = width;
  int h = height;
  uint32_t *fb = (uint32_t *)(uintptr_t)FB_ADDR;
  // for (i = 0; i < w * h; i++) fb[i] = i;
  // for (i = 0; i < w * h; i++) fb[i] = 0x00ffffff;
  for (i = 0; i < w * h; i++) fb[i] = 0x00000000;
  outl(SYNC_ADDR, 1);
}

void __am_gpu_config(AM_GPU_CONFIG_T *cfg) {
  *cfg = (AM_GPU_CONFIG_T) {
    .present = true,
    .has_accel = false,
    .width = width,
    .height = height,
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

  int pixel_num_x = (w < (width - x) ? w : (width - x));
  int pixel_byt_x = pixel_num_x * sizeof(uint32_t);
  for (int j = 0; j < h && y + j < height; j++) {
    int index = (y + j) * width + x;
    memcpy(&fb[index], pixels, pixel_byt_x);
    pixels += w;
  }

  if (ctl->sync) {
    outl(SYNC_ADDR, 1);
  }
}

void __am_gpu_status(AM_GPU_STATUS_T *status) {
  status->ready = true;
}
