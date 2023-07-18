#define SDL_malloc  malloc
#define SDL_free    free
#define SDL_realloc realloc

#define SDL_STBIMAGE_IMPLEMENTATION
#include "SDL_stbimage.h"

SDL_Surface* IMG_Load_RW(SDL_RWops *src, int freesrc) {
  assert(src->type == RW_TYPE_MEM);
  assert(freesrc == 0);
  return NULL;
}

SDL_Surface* IMG_Load(const char *filename) {
  // FILE *fp = fopen(filename, "r");
  // assert(fp);

  // fseek(fp, 0, SEEK_END);
  // long size = ftell(fp);
  // char *buf = SDL_malloc(size);
  // fseek(fp, 0, SEEK_SET);
  // fread(buf, 1, size, fp);

  // SDL_Surface *surface_p = STBIMG_LoadFromMemory(buf, size);
  // assert(surface_p);

  // fclose(fp);
  // SDL_free(buf);
  // return surface_p;



  FILE *imageFile = fopen(filename, "r");
  assert(imageFile != NULL);

  int imageSize = -1;
  fseek(imageFile, 0, SEEK_END);
  imageSize = ftell(imageFile);
  //printf("[SDL_image] imageSize = %d\n", imageSize);
  assert(imageSize >= 0);

  void *buf = SDL_malloc(imageSize);
  assert(buf != NULL);
  fseek(imageFile, 0, SEEK_SET);
  fread(buf, 1, imageSize, imageFile);

  //printf("[SDL_image] buffer copy complete\n");

  SDL_Surface* ret = STBIMG_LoadFromMemory(buf, imageSize);
  assert(ret != NULL);

  //printf("[SDL_image] going to close file\n");
  fclose(imageFile);
  //printf("[SDL_image] image file colsed\n");
  free(buf);
  //printf("[SDL_image] buffer free OK\n");
  return ret;
}

int IMG_isPNG(SDL_RWops *src) {
  return 0;
}

SDL_Surface* IMG_LoadJPG_RW(SDL_RWops *src) {
  return IMG_Load_RW(src, 0);
}

char *IMG_GetError() {
  return "Navy does not support IMG_GetError()";
}
