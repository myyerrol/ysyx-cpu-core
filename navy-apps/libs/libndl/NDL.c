#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/time.h>
#include <fcntl.h>
#include <assert.h>

static int evtdev = -1;
static int fbdev = -1;
static int screen_w = 0, screen_h = 0;

uint32_t NDL_GetTicks() {
  struct timeval timer;
  gettimeofday(&timer, NULL);
  return timer.tv_sec * 1000;
}

int NDL_PollEvent(char *buf, int len) {
  int fd = open("/dev/events", 0, 0);
  return read(fd, buf, len);
}

void NDL_OpenCanvas(int *w, int *h) {
  int fd = open("/proc/dispinfo", 0, 0);

  char buf[64];
  read(fd, buf, sizeof(buf));
  sscanf(buf, "width: %d, height: %d", &screen_w, &screen_h);

  if (*w == 0 && *h == 0) {
    *w = screen_w;
    *h = screen_h;
  }
  assert(screen_w >= *w && screen_h >= *h);
  close(fd);

  // if (getenv("NWM_APP")) {
  //   int fbctl = 4;
  //   fbdev = 5;
  //   screen_w = *w; screen_h = *h;
  //   char buf[64];
  //   int len = sprintf(buf, "%d %d", screen_w, screen_h);
  //   // let NWM resize the window and create the frame buffer
  //   write(fbctl, buf, len);
  //   while (1) {
  //     // 3 = evtdev
  //     int nread = read(3, buf, sizeof(buf) - 1);
  //     if (nread <= 0) continue;
  //     buf[nread] = '\0';
  //     if (strcmp(buf, "mmap ok") == 0) break;
  //   }
  //   close(fbctl);
  // }
}

void NDL_DrawRect(uint32_t *pixels, int x, int y, int w, int h) {
  x = 1;
  y = screen_w * ((screen_h - h) / 2) + ((screen_w - w) / 2);

  int fd = open("/dev/fb", 0, 0);
  lseek(fd, x * y, SEEK_SET);
  write(fd, pixels, ((size_t)w << 32) | ((size_t)h & 0x00000000FFFFFFFF));
  close(fd);
}

void NDL_OpenAudio(int freq, int channels, int samples) {
}

void NDL_CloseAudio() {
}

int NDL_PlayAudio(void *buf, int len) {
  return 0;
}

int NDL_QueryAudio() {
  return 0;
}

int NDL_Init(uint32_t flags) {
  if (getenv("NWM_APP")) {
    evtdev = 3;
  }
  return 0;
}

void NDL_Quit() {
}
