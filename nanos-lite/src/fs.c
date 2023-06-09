#include <fs.h>

extern size_t ramdisk_read(void *buf, size_t offset, size_t len);

size_t invalid_read(void *buf, size_t offset, size_t len) {
  panic("should not reach here");
  return 0;
}

size_t invalid_write(const void *buf, size_t offset, size_t len) {
  panic("should not reach here");
  return 0;
}

/* This is the information about all files in disk. */
static Finfo file_table[] __attribute__((used)) = {
  [FD_STDIN]  = {"stdin", 0, 0, invalid_read, invalid_write},
  [FD_STDOUT] = {"stdout", 0, 0, invalid_read, invalid_write},
  [FD_STDERR] = {"stderr", 0, 0, invalid_read, invalid_write},
#include "files.h"
};

void init_fs() {
  // TODO: initialize the size of /dev/fb
}

Finfo fs_get(int fd) {
  return file_table[fd];
}

int fs_open(const char *pathname, int flags, int mode) {
  int fd = -1;
  for (int i = 0; i < LENGTH(file_table); i++) {
    if (strcmp(file_table[i].name, pathname) == 0) {
      fd = i;
      break;
    }
  }
  if (fd == -1) {
    assert(0);
  }
  return fd;
}

int fs_close(int fd) {

  return 0;
}

size_t fs_read(int fd, void *buf, size_t len) {
  if (fd > 2 && fd < LENGTH(file_table)) {
    Finfo file = file_table[fd];
    if (file.open_offset + len > file.size) {
      len = file.size - file.open_offset;
    }
    size_t bytes = ramdisk_read(buf, file.disk_offset + file.open_offset, len);
    file.open_offset = bytes;
    return bytes;
  }
  else {
    return -1;
  }
}

size_t fs_write(int fd, const void *buf, size_t len) {
  return -1;
}

size_t fs_lseek(int fd, size_t offset, int wnehce) {
  if (fd > 2 && fd < LENGTH(file_table)) {
    Finfo *file = &file_table[fd];
    switch (wnehce) {
      case SEEK_SET: {
        (*file).open_offset = offset;
        break;
      }
      case SEEK_CUR: {
        (*file).open_offset += offset;
        break;
      }
      case SEEK_END: {
        (*file).open_offset = (*file).size - offset;
        break;
      }
      default: {
        (*file).open_offset = 0;
        break;
      }
    }
    (*file).open_offset = ((*file).open_offset < (*file).size) ?
                           (*file).open_offset : (*file).size;
    return 0;
  }
  else {
    return -1;
  }
}
