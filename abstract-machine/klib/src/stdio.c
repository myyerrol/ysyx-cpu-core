#include <stdarg.h>

#include <am.h>
#include <klib.h>
#include <klib-macros.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

int  num_place_i = 0;
char num_place_arr[256];

int getNumLen(int num) {
  int len = 0;
  if (num == 0) {
    len = 1;
  }
  else {
    for (; num > 0; ++len) {
      num /= 10;
    }
  }
  return len;
}

void printfChar(char ch) {
  putch(ch);
}

void printfCharPlace(int num_real) {
  char num_place_char = '0';
  int  num_place_len  =  0;

  int i = 0;
  // 计算占位数字的大小（代表需要占几位数）
  while (num_place_i != 0) {
    int num_place = num_place_arr[i] - '0';
    int k = (num_place_i == 5) ? 10000 :
            (num_place_i == 4) ? 1000  :
            (num_place_i == 3) ? 100   :
            (num_place_i == 2) ? 10    :
            (num_place_i == 1) ? 1     : 0;
    num_place_len = num_place_len + num_place * k;
    i++;
    num_place_i--;
  }

  // 获取原始数字的位数
  int num_real_len = getNumLen(num_real);
  int num_delta = num_place_len - num_real_len;
  while (num_delta > 0) {
    putch(num_place_char);
    num_delta--;
  }
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

  printfCharPlace(dec);

  if (dec != 0) {
    printfNum(dec, 10);
  }
  else {
    putch('0');
  }
}

void printfOct(uint32_t oct) {
  printfCharPlace(oct);

  if (oct != 0) {
    printfNum(oct, 8);
  }
  else {
    putch('0');
  }
}

void printfHex(uint32_t hex) {
  printfCharPlace(hex);

  if (hex != 0) {
    printfNum(hex, 16);
  }
  else {
    putch('0');
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

  num_place_i = 0;
  memset(num_place_arr, -1, 256);

  while (*fmt != '\0') {
    if (*fmt == '%') {
      fmt++;
      while (*fmt >= '0' && *fmt <= '9') {
        num_place_arr[num_place_i] = *fmt;
        num_place_i++;
        fmt++;
      }
      switch (*fmt) {
        case 'c': printfChar (va_arg(va_ptr, int));      break;
        case 's': printfStr  (va_arg(va_ptr, char*));    break;
        case 'd': printfDec  (va_arg(va_ptr, int));      break;
        case 'o': printfOct  (va_arg(va_ptr, uint32_t)); break;
        case 'x': printfHex  (va_arg(va_ptr, uint32_t)); break;
        case 'p': printfAddr (va_arg(va_ptr, uint64_t)); break;
        case 'f': printfFloat(va_arg(va_ptr, double));   break;
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
        case 'd': printfDecS(va_arg(va_ptr, int));   break;
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
