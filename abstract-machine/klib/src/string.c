#include <klib.h>
#include <klib-macros.h>
#include <stdint.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)



void *memset(void *s, int c, size_t n) {
  // assert(s != NULL);
  size_t i = 0;
  char *res = (char *)s;
  while (*res != '\0' && i++ < n) {
      *res++ = c;
  }
  return s;
}






size_t strlen(const char *s) {
  // assert(s != NULL);
  size_t len = 0;
  while (*s++ != '\0') {
      len++;
  }
  return len;
}

char *strcpy(char *dst, const char *src) {
  // assert((dst != NULL) && (src != NULL));
  char *res = dst;
  while ((*dst++ = *src++) != '\0');
  return res;
}

char *strncpy(char *dst, const char *src, size_t n) {
  // assert((dst != NULL) && (src != NULL));
  char *res = dst;
  size_t i = 0;
  while ((*dst++ = *src++) != '\0' && i++ < n);
  if (*(--dst) != '\0') {
      *dst = '\0';
  }
  return res;
}

char *strcat(char *dst, const char *src) {
  // assert((dst != NULL) && (src != NULL));
  char *res = dst;
  dst = dst + strlen(dst);
  while ((*dst++ = *src++) != '\0');
  return res;
}

int strcmp(const char *s1, const char *s2) {
  // assert((s1 != NULL) && (s2 != NULL));
  while (*s1 == *s2) {
      if (*s1 == '\0') {
          return 0;
      }
      else {
          s1++;
          s2++;
      }
  }
  return *s1 - *s2;
}

int strncmp(const char *s1, const char *s2, size_t n) {
  // assert((s1 != NULL) && (s2 != NULL));
  size_t i = 0;
  while (i++ < n) {
      if (*s1 == *s2) {
          s1++;
          s2++;
      }
      else {
          return *s1 - *s2;
      }
  }
  return 0;
}

void *memmove(void *dst, const void *src, size_t n) {
  return strncpy(dst, src, n);
}

void *memcpy(void *out, const void *in, size_t n) {
  return strncpy(out, in, n);
}

int memcmp(const void *s1, const void *s2, size_t n) {
  return strncmp(s1, s2, n);
}

#endif
