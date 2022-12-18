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
#define WATCH_TEST_CHAR_LEN  8 + 1
#define WATCH_TEST_SPACE_LEN 5
#define WATCH_TEST_LEN WATCH_TEST_CHAR_LEN + WATCH_TEST_SPACE_LEN

#define INFO_LINKED_LIST 1

typedef struct watchpoint {
  /* TODO: Add more members if necessary */
  int no;
  char *expr;
  word_t val;
  struct watchpoint *next;
} WP;

static WP wp_pool[NR_WP] = {};
static WP *wp_head = NULL, *wp_free = NULL;

void init_wp_pool() {
  int i;
  for (i = 0; i < NR_WP; i ++) {
    wp_pool[i].no = i;
    wp_pool[i].val = 0;
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
    wp_new->expr = (char *)malloc(sizeof(char) * WATCH_ARR_LENGTH);
    strcpy(wp_new->expr, expr);
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
        wp_prev->next = wp_temp->next;
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
    free(wp_temp->expr);
    wp_temp->val = 0;
  }
  else {
    printf("No breakpoint number %d.\n", no);
  }
}

void watch_new(char *expr) {
  WP *wp_new = new_wp(expr);
  printf("Hardware watchpoint %d: %s\n", wp_new->no, wp_new->expr);
}

void watch_free(int no) {
  free_wp(no);
}

void watch_display() {
  if (wp_head != NULL) {
    char *line = NULL;
    char *line_no = NULL;
    char *line_arrow = NULL;
    char *line_next = NULL;

    printf("Num%5sType%10sDisp Enb Address%10sWhat\n", " ", " ", " ");

    WP *wp_temp = wp_head;
    while (wp_temp != NULL) {
      int no = wp_temp->no;
      char *expr = wp_temp->expr;

      printf("%-2d%6shw watchpoint keep y%20s%s\n", no, " ", " ", expr);

      char *line_no_t = malloc(sizeof(char) * WATCH_TEST_CHAR_LEN);
      sprintf(line_no_t, "|  %02d  |", no);
      if (line == NULL) {
        line = (char *)malloc(sizeof(char) * WATCH_TEST_LEN * NR_WP);
        strcat(line, "--------");

        line_no = malloc(sizeof(char) * WATCH_TEST_LEN * NR_WP);
        strcat(line_no, line_no_t);

        line_arrow = malloc(sizeof(char) * WATCH_TEST_LEN * NR_WP);
        strcat(line_arrow, "--------");

        line_next = malloc(sizeof(char) * WATCH_TEST_LEN * NR_WP);
        strcat(line_next, "| next |");
      }
      else {
        strcat(line, "     --------");

        char *line_no_tt = malloc(sizeof(char) * WATCH_TEST_LEN);
        sprintf(line_no_tt, "     %s", line_no_t);
        strcat(line_no, line_no_tt);
        free(line_no_tt);

        strcat(line_arrow, " ——> --------");

        strcat(line_next, "     | next |");
      }
      free(line_no_t);

      wp_temp = wp_temp->next;
    }

#if INFO_LINKED_LIST
    printf("wp_head: \n");
    printf("%s\n", line);
    printf("%s\n", line_no);
    printf("%s\n", line_arrow);
    printf("%s\n", line_next);
    printf("%s\n", line);
#endif
  }
  else {
    printf("No watchpoints.\n");
  }
}

void watch_trace() {
  int count = 0;
  WP *wp_temp = wp_head;
  while (wp_temp != NULL) {
    int no = wp_temp->no;
    char *expr_str = wp_temp->expr;
    word_t val_old = wp_temp->val;
    bool success = false;
    word_t val_new = expr(expr_str, NULL, &success);
    if (val_new != val_old) {
      count++;
      if (count > 1) {
        printf("\n");
      }
      printf("Hardware watchpoint %d: %s\n", no, expr_str);
      printf("Value Old = %lu\n", val_old);
      printf("value New = %lu\n", val_new);
      wp_temp->val = val_new;
    }
    wp_temp = wp_temp->next;
  }
}

void watch_test() {
  new_wp("*0x80000000");
  new_wp("*0x80000004");
  watch_display();
}
