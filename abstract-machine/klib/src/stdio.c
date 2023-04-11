#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

void printfChar(char ch) {
  putch(ch);
}

void printfStr(char *str) {
  putstr(str);
}

void printfNum(uint64_t num, int base) {
  if (num == 0) {
    return;
  }
  else {
    printfNum(num / base, base);
    putch("0123456789abcdef"[num % base]);
  }
}

void printfDec(int dec) {
  if (dec < 0) {
    putch('-');
    dec = -dec;
  }
  if (dec != 0) {
    printfNum(dec, 10);
  }
  else {
    putch('0');
    return;
  }
}

void printfOct(uint32_t oct) {
  if (oct != 0) {
    printfNum(oct, 8);
  }
  else {
    putch('0');
    return;
  }
}

void printfHex(uint32_t hex) {
  if (hex != 0) {
    printfNum(hex, 16);
  }
  else {
    putch('0');
    return;
  }
}

void printfAddr(uint64_t addr) {
  putch('0');
  putch('x');
  printfNum(addr, 16);
}

void printfFloat(double num) {
  int num_int;

  num_int = (int)num;
  printfNum(num_int, 10);

  putch('.');

  num -= num_int;
  if (num != 0) {
    num_int = (int)(num * 1000000);
    printfNum(num_int, 10);
  }
  else {
    for (num_int = 0; num_int < 6; num_int++) {
      putch('0');
    }
  }
}

int printf(const char *fmt, ...) {
  va_list va_ptr;
  va_start(va_ptr, fmt);

  while (*fmt != '\0') {
    if (*fmt == '%') {
      fmt++;
      switch (*fmt) {
        case 'c': printfChar(va_arg(va_ptr, int)); break;
        case 's': printfStr(va_arg(va_ptr, char*)); break;
        case 'd': printfDec(va_arg(va_ptr, int)); break;
        case 'o': printfOct(va_arg(va_ptr, uint32_t)); break;
        case 'x': printfHex(va_arg(va_ptr, uint32_t)); break;
        case 'p': printfAddr(va_arg(va_ptr, uint64_t)); break;
        case 'f': printfFloat(va_arg(va_ptr, double)); break;
        default: break;
      }
    }
    else {
      putch(*fmt);
    }
    fmt++;
  }

  va_end(va_ptr);

  return 0;
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







int vsprintf(char *out, const char *fmt, va_list ap) {
  panic("Not implemented");
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
