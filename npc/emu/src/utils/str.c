#include <utils/str.h>

char *strrpc(char *str, char *str_old, char *str_new) {
    char str_buf[strlen(str)];
    memset(str_buf, 0, sizeof(str_buf));
    for (int i = 0; i < strlen(str); i++) {
        if (!strncmp(str + i, str_old, strlen(str_old))) {
            strcat(str_buf, str_new);
            i += strlen(str_old) - 1;
        }
        else {
            strncat(str_buf, str + i, 1);
        }
    }
    strcpy(str, str_buf);
    return str;
}
