#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

int printf(const char *fmt, ...) {
  panic("Not implemented");
}

int vsprintf(char *out, const char *fmt, va_list ap) {
  panic("Not implemented");
}

void itoa(unsigned int n, char *buf) {
  int i;
  if (n < 10) {
    buf[0] = n + '0';
    buf[1] = '\0';
    return;
  }
  itoa(n / 10, buf);

  for (i = 0; buf[i] != '\0'; i++);

  buf[i] = (n % 10) + '0';
  buf[i + 1] = '\0';
}

int sprintf(char *out, const char *fmt, ...) {
  int arg_num;
  char *arg_str;

  char buf[128];
  memset(buf, 0, sizeof(buf));

  va_list ap;
  va_start(ap, fmt);

  while (*fmt != '\0') {
    if (*fmt == '%') {
      fmt++;
      switch (*fmt) {
        case 's': {
          arg_str = va_arg(ap, char*);
          memcpy(out, arg_str, strlen(arg_str));
          out += strlen(arg_str);
          break;
        }
        case 'd': {
          arg_num = va_arg(ap, int);
          if (arg_num < 0) {
              *out = '-';
              out++;
              arg_num = -arg_num;
          }
          itoa(arg_num, buf);
          // buf[0] = '1';
          // buf[1] = '0';
          // buf[2] = '\0';
          memcpy(out, buf, strlen(buf));
          out += strlen(buf);
          break;
        }
        default: {
          break;
        }
      }
    }
    else {
      *out = *fmt;
      out++;
    }
    fmt++;
  }

  *out = '\0';
  va_end(ap);

  return 0;
}

int snprintf(char *out, size_t n, const char *fmt, ...) {
  panic("Not implemented");
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) {
  panic("Not implemented");
}

#endif
