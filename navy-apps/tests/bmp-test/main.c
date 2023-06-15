#include <stdio.h>
#include <assert.h>
#include <stdlib.h>
#include <NDL.h>
#include <BMP.h>

#define WIDTH  400
#define HEIGHT 300

int main() {
  NDL_Init(0);
  int w, h;
  void *bmp = BMP_Load("/share/pictures/projectn.bmp", &w, &h);
  assert(bmp);
  NDL_OpenCanvas(&w, &h);
  int x = 1;
  int y = WIDTH * ((HEIGHT - h) / 2) + ((WIDTH - w) / 2);
  NDL_DrawRect(bmp, x, y, w, h);
  free(bmp);
  NDL_Quit();
  printf("Test ends! Spinning...\n");
  while (1);
  return 0;
}
