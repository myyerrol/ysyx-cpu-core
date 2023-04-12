#include <klib.h>
#include <klib-macros.h>
#include <stdint.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

void *memset(void *s, int c, size_t n) {
  assert(s != NULL);
  size_t i = 0;
  char *s_t = (char *)s;
  while (*s_t != '\0' && i++ < n) {
    *s_t++ = c;
  }
  return s;
}

void *memcpy(void *dst, const void *src, size_t n) {
  assert((dst != NULL) && (src != NULL));
  char *dst_t = (char *)dst;
  char *src_t = (char *)src;
  while (n--) {
    *dst_t++ = *src_t++;
  }
  return dst;
}

void *memmove(void *dst, const void *src, size_t n) {
  assert((dst != NULL) && (src != NULL));
  char *dst_t = (char *)dst;
  char *src_t = (char *)src;
  if (((dst + n) < src) || ((src + n) < dst)) {
    // 没有重叠，正向拷贝
    while (n--) {
      *dst_t++ = *src_t++;
    }
  }
  else {
    // 存在重叠，逆向拷贝
    dst_t += (n - 1);
    src_t += (n - 1);
    while (n--) {
      *dst_t-- = *src_t--;
    }
  }
  return dst;
}

int memcmp(const void *s1, const void *s2, size_t n) {
  assert((s1 != NULL) && (s2 != NULL));
  size_t i = 0;
  char *s1_t = (char *)s1;
  char *s2_t = (char *)s2;
  while (i++ < n) {
    if (*s1_t == *s2_t) {
      s1_t++;
      s2_t++;
    }
    else {
      return *s1_t - *s2_t;
    }
  }
  return 0;
}

size_t strlen(const char *s) {
  assert(s != NULL);
  size_t len = 0;
  while (*s++ != '\0') {
    len++;
  }
  return len;
}

char *strcat(char *dst, const char *src) {
  assert((dst != NULL) && (src != NULL));
  char *dst_t = dst;
  dst = dst + strlen(dst);
  while ((*dst++ = *src++) != '\0');
  return dst_t;
}

char *strcpy(char *dst, const char *src) {
  assert((dst != NULL) && (src != NULL));
  char *dst_t = dst;
  while ((*dst++ = *src++) != '\0');
  return dst_t;
}

char *strncpy(char *dst, const char *src, size_t n) {
  assert((dst != NULL) && (src != NULL));
  char *dst_t = dst;
  size_t i = 0;
  while ((*dst++ = *src++) != '\0' && i++ < n);
  if (*(--dst) != '\0') {
    *dst = '\0';
  }
  return dst_t;
}

int strcmp(const char *s1, const char *s2) {
  assert((s1 != NULL) && (s2 != NULL));
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
  return memcmp(s1, s2, n);
}

#endif
