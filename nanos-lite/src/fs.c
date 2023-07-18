#include <fs.h>

extern size_t ramdisk_read(void *buf, size_t offset, size_t len);
extern size_t ramdisk_write(const void *buf, size_t offset, size_t len);
extern size_t serial_write(const void *buf, size_t offset, size_t len);
extern size_t events_read(void *buf, size_t offset, size_t len);
extern size_t dispinfo_read(void *buf, size_t offset, size_t len);
extern size_t fb_write(const void *buf, size_t offset, size_t len);

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
  [FD_STDIN]    = { "stdin",          0, 0, invalid_read,  invalid_write },
  [FD_STDOUT]   = { "stdout",         0, 0, invalid_read,  serial_write  },
  [FD_STDERR]   = { "stderr",         0, 0, invalid_read,  serial_write  },
  [FD_EVENTS]   = { "/dev/events",    0, 0, events_read,   invalid_write },
  [FD_DISPINFO] = { "/proc/dispinfo", 0, 0, dispinfo_read, invalid_write },
  [FD_FB]       = { "/dev/fb",        0, 0, invalid_read,  fb_write,     },
#include "files.h"
};

size_t open_offset = 0;

void init_fs() {
  AM_GPU_CONFIG_T cfg = io_read(AM_GPU_CONFIG);
  file_table[FD_FB].size = cfg.width * cfg.height;
}

Finfo fs_get(int fd) {
  return file_table[fd];
}

int fs_open(const char *pathname, int flags, int mode) {
  int fd = -1;
  for (int i = 0; i < LENGTH(file_table); i++) {
    if (strcmp(file_table[i].name, pathname) == 0) {
      fd = i;
      open_offset = 0;
      break;
    }
  }
  if (fd == -1) {
    panic("Can not open file");
  }
  return fd;
}

int fs_close(int fd) {
  open_offset = 0;
  return 0;
}

size_t fs_read(int fd, void *buf, size_t len) {
    // if (fd == 0 || fd > 2) {
  //   Finfo *file = &file_table[fd];
  //   if ((open_offset + len) > file->size) {
  //     len = file->size - open_offset;
  //   }
  //   size_t offset = file->disk_offset + open_offset;
  //   size_t bytes = ramdisk_read(buf, offset, len);
  //   open_offset += bytes;
  //   return bytes;
  // }
  // else {
  //   return -1;
  // }

  Finfo *file = &file_table[fd];
  if (file->read != NULL) {
    return file->read(buf, 0, len);
  }
  else {
    size_t file_size = file_table[fd].size;
    if ((open_offset + len) > file_size) {
      len = file_size - open_offset;
    }
    ramdisk_read(buf, file->disk_offset + open_offset, len);
    open_offset = open_offset + len;
    return len;
  }
}

size_t fs_write(int fd, const void *buf, size_t len) {
  // if (fd == 1 || fd == 2) {
  //   size_t i = 0;
  //   for(; len > 0; len--) {
  //     putch(((char*)buf)[i]);
  //     i++;
  //   }
  //   return i;
  // }
  // else if (fd != 0) {
  //   Finfo *file = &file_table[fd];
  //   if (open_offset + len > file->size) {
  //     len = file->size - open_offset;
  //   }
  //   size_t offset = file->disk_offset + open_offset;
  //   size_t bytes = ramdisk_write(buf, offset, len);
  //   open_offset += bytes;
  //   return bytes;
  // }
  // else {
  //   return -1;
  // }

  Finfo *file = &file_table[fd];
  if (file->write != NULL) {
    return file->write(buf, open_offset, len);
  }
  else {
    size_t file_size = file->size;
    if ((open_offset + len) > file_size) {
      len = file_size - open_offset;
    }
    ramdisk_write(buf, file->disk_offset + open_offset, len);
    open_offset = open_offset + len;
    return len;
  }
}

size_t fs_lseek(int fd, size_t offset, int whence) {
  if (fd > 2) {
    Finfo *file = &file_table[fd];
    size_t file_size = file->size;
    switch (whence) {
      case SEEK_SET: {
        if (offset <= file_size) {
          open_offset = offset;
          return open_offset;
        }
        else {
          return -1;
        }
        break;
      }
      case SEEK_CUR: {
        if (open_offset + offset <= file_size) {
          open_offset = open_offset + offset;
          return open_offset;
        }
        else {
          return -1;
        }
        break;
      }
      case SEEK_END: {
        if((signed)offset <= 0) {
          open_offset = file_size + offset;
          return open_offset;
        }
        else {
          return -1;
        }
        break;
      }
      default: {
        panic("Not implemented");
        break;
      }
    }
  }
  else {
    return -1;
  }
}
