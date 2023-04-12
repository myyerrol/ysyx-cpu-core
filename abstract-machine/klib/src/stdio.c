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

char *out_g = NULL;
char  num_arr[256];
char *num_arr_p = NULL;

void printfStrS(char *str) {
  memcpy(out_g, str, strlen(str));
  out_g += strlen(str);
}

void printfNumS(uint64_t num, int base) {
  if (num == 0) {
    return;
  }
  else {
    printfNumS(num / base, base);
    char num_ch = "0123456789abcdef"[num % base];
    // printf("num_ch: %c\n", num_ch);
    *num_arr_p = num_ch;
    num_arr_p++;
  }
}

void printfDecS(int dec) {
  if (dec < 0) {
    *out_g = '-';
    out_g++;
    dec = -dec;
  }
  if (dec != 0) {
    memset(num_arr, '\0', 256);
    num_arr_p = num_arr;
    printfNumS(dec, 10);
    memcpy(out_g, num_arr, strlen(num_arr));
    out_g += strlen(num_arr);
  }
  else {
    *out_g = '0';
    out_g++;
  }
}

int sprintf(char *out, const char *fmt, ...) {
  va_list va_ptr;
  va_start(va_ptr, fmt);

  out_g = out;

  while (*fmt != '\0') {
    if (*fmt == '%') {
      fmt++;
      switch (*fmt) {
        case 's': printfStrS(va_arg(va_ptr, char*)); break;
        case 'd': printfDecS(va_arg(va_ptr, int)); break;
        default: break;
      }
    }
    else {
      *out_g = *fmt;
      out_g++;
    }
    fmt++;
  }

  *out_g = '\0';
  va_end(va_ptr);
  // printf("out: %s\n", out);
  return 0;
}

int vsprintf(char *out, const char *fmt, va_list va_ptr) {
  panic("Not implemented");
}

int snprintf(char *out, size_t n, const char *fmt, ...) {
  panic("Not implemented");
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list va_ptr) {
  panic("Not implemented");
}

#endif
