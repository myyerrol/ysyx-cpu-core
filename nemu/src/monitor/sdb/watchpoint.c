/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include "sdb.h"

#define NR_WP 32
#define WATCH_ARR_LENGTH 256

typedef struct watchpoint {
  /* TODO: Add more members if necessary */
  int no;
  char *expr;
  char *val_old;
  char *val_new;
  struct watchpoint *next;
} WP;

static WP wp_pool[NR_WP] = {};
static WP *wp_head = NULL, *wp_free = NULL;

void init_wp_pool() {
  int i;
  for (i = 0; i < NR_WP; i ++) {
    wp_pool[i].no = i;
    wp_pool[i].expr = '\0';
    wp_pool[i].val_old = '\0';
    wp_pool[i].val_new = '\0';
    wp_pool[i].next = (i == NR_WP - 1 ? NULL : &wp_pool[i + 1]);
  }

  wp_head = NULL;
  wp_free = wp_pool;
}

/* TODO: Implement the functionality of watchpoint */
static WP *new_wp(char *expr) {
  WP *wp_new = NULL;

  // 获取空闲链表
  if (wp_free != NULL) {
    wp_new = wp_free;
    wp_free = wp_free->next;
    wp_new->expr = expr;
    // wp_new->val_old = val_old;
    // wp_new->val_new = val_new;
    wp_new->next = NULL;
  }
  else {
    assert(0);
  }

  // 将空闲链表添加到监视链表后面
  if (wp_head != NULL) {
    WP *wp_temp = wp_head;
    while (wp_temp->next != NULL) {
      wp_temp = wp_temp->next;
    }
    wp_temp->next = wp_new;
  }
  else {
    wp_head = wp_new;
  }

  return wp_new;
}

static void free_wp(int no) {
  WP *wp_old = NULL;
  WP *wp_prev = wp_head;
  WP *wp_temp = wp_head;

  // 获取释放链表
  while (wp_temp != NULL) {
    if (wp_temp->no == no) {
      if (wp_temp == wp_head) {
        wp_head = wp_head->next;
        wp_temp->next = NULL;
        wp_old = wp_temp;
      }
      else {
        wp_prev->next = NULL;
        wp_temp->next = NULL;
        wp_old = wp_temp;
      }
      break;
    }
    wp_prev = wp_temp;
    wp_temp = wp_temp->next;
  }

  // 将释放链表添加到空闲链表后面并重置链表中的相关数据
  if (wp_old != NULL) {
    if (wp_free != NULL) {
      wp_temp = wp_free;
      while (wp_temp->next != NULL) {
        wp_temp = wp_temp->next;
      }
      wp_temp->next = wp_old;
    }
    else {
      wp_temp = wp_old;
      wp_free = wp_temp;
    }
    wp_temp->expr = '\0';
    wp_temp->val_old = '\0';
    wp_temp->val_new = '\0';
  }
  else {
    assert(0);
  }
}

int watch_new(char *expr) {
  WP *wp_new = new_wp(expr);
  return wp_new->no;
}

void watch_free(int no) {
  free_wp(no);
}

void watch_display() {
  if (wp_head != NULL) {
    printf("Num%5sType%10sDisp Enb Address%10sWhat\n", " ", " ", " ");
    WP *wp_temp = wp_head;
    while (wp_temp != NULL) {
      int no = wp_temp->no;
      char *expr = wp_temp->expr;
      printf("%-2d%6shw watchpoint keep y%20s%s\n", no, " ", " ", expr);
      wp_temp = wp_temp->next;
    }
  }
  else {
    printf("No watchpoints.\n");
  }
}

void watch_test() {
  new_wp("1+2");
  new_wp("0x80000000");
  watch_display();
}
