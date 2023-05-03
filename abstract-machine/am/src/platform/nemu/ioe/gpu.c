#include <am.h>
#include <nemu.h>
#include <klib.h>

#define SYNC_ADDR (VGACTL_ADDR + 4)

void __am_gpu_init() {
  uint32_t gpu_cfg = inl(VGACTL_ADDR);
  uint32_t width  = (gpu_cfg & 0xffff0000) >> 16;
  uint32_t height = (gpu_cfg & 0x0000ffff);
  int i;
  int w = width;
  int h = height;
  uint32_t *fb = (uint32_t *)(uintptr_t)FB_ADDR;
  for (i = 0; i < w * h; i++) fb[i] = i;
  outl(SYNC_ADDR, 1);
}

void __am_gpu_config(AM_GPU_CONFIG_T *cfg) {
  uint32_t gpu_cfg = inl(VGACTL_ADDR);
  uint32_t width  = (gpu_cfg & 0xffff0000) >> 16;
  uint32_t height = (gpu_cfg & 0x0000ffff);
  *cfg = (AM_GPU_CONFIG_T) {
    .present = true, .has_accel = false,
    .width = width, .height = height,
    .vmemsz = 0
  };
}

void __am_gpu_fbdraw(AM_GPU_FBDRAW_T *ctl) {



  if (ctl->sync) {
    outl(SYNC_ADDR, 1);
  }
}

void __am_gpu_status(AM_GPU_STATUS_T *status) {
  status->ready = true;
}
