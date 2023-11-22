#include <monitor/sdb/expr.h>
#include <monitor/sdb/watch.h>

#define NR_WP 32
#define WATCH_ARR_LENGTH 256

typedef struct watchpoint {
    int no;
    char *expr;
    word_t val;
    struct watchpoint *next;
} WP;

static WP  wp_pool[NR_WP] = {};
static WP *wp_head = NULL, *wp_free = NULL;

static void freeSDBWatchPool(int no) {
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
        LOG_BRIEF("[sdb] [watch] no breakpoint number %d", no);
    }
}

static WP *newSDBWatchPool(char *expr) {
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

void freeSDBWatch(int no) {
    freeSDBWatchPool(no);
}

void initSDBWatch() {
    int i;
    for (i = 0; i < NR_WP; i ++) {
        wp_pool[i].no = i;
        wp_pool[i].val = 0;
        wp_pool[i].next = (i == NR_WP - 1 ? NULL : &wp_pool[i + 1]);
    }
    wp_head = NULL;
    wp_free = wp_pool;
}

void newSDBWatch(char *expr) {
    WP *wp_new = newSDBWatchPool(expr);
    LOG_BRIEF("[sdb] [watch] hardware watchpoint %d: %s", wp_new->no, wp_new->expr);
}

void printfSDBWatch() {
    if (wp_head != NULL) {
        char *line = NULL;
        char *line_no = NULL;
        char *line_arrow = NULL;
        char *line_next = NULL;

        LOG_BRIEF("[sdb] [watch]");
        LOG_BRIEF("Num%5sType%10sDisp Enb Address%10sWhat\n", " ", " ", " ");

        WP *wp_temp = wp_head;
        while (wp_temp != NULL) {
            int no = wp_temp->no;
            char *expr = wp_temp->expr;

            LOG_BRIEF("%-2d%6shw watchpoint keep y%20s%s\n", no, " ", " ", expr);

            char *line_no_t = (char *)malloc(sizeof(char) * WATCH_ARR_LENGTH);
            sprintf(line_no_t, "|  %02d  |", no);
            if (line == NULL) {
                line = (char *)malloc(sizeof(char) * WATCH_ARR_LENGTH * NR_WP);
                strcat(line, "--------");

                line_no = (char *)malloc(sizeof(char) * WATCH_ARR_LENGTH * NR_WP);
                strcat(line_no, line_no_t);

                line_arrow = (char *)malloc(sizeof(char) * WATCH_ARR_LENGTH * NR_WP);
                strcat(line_arrow, "--------");

                line_next = (char *)malloc(sizeof(char) * WATCH_ARR_LENGTH * NR_WP);
                strcat(line_next, "| next |");
            }
            else {
                strcat(line, "     --------");

                char *line_no_tt = (char *)malloc(sizeof(char) * WATCH_ARR_LENGTH);
                sprintf(line_no_tt, "     %s", line_no_t);
                strcat(line_no, line_no_tt);
                free(line_no_tt);

                strcat(line_arrow, " ——> --------");

                strcat(line_next, "     | next |");
            }
            free(line_no_t);

            wp_temp = wp_temp->next;
        }

        LOG_BRIEF();
        LOG_BRIEF("%s", line);
        LOG_BRIEF("%s", line_no);
        LOG_BRIEF("%s", line_arrow);
        LOG_BRIEF("%s", line_next);
        LOG_BRIEF("%s", line);
        LOG_BRIEF();

        free(line);
        free(line_no);
        free(line_arrow);
        free(line_next);
    }
    else {
        LOG_BRIEF("[sdb] [watch] no watchpoints");
    }
}

void testSDBWatch() {
    newSDBWatchPool((char *)"*0x80000000");
    newSDBWatchPool((char *)"*0x80000004");
    newSDBWatchPool((char *)"*0x80000008");
    printfSDBWatch();
}

int traceSDBWatch() {
    int count = 0;
    WP *wp_temp = wp_head;

    while (wp_temp != NULL) {
        int no = wp_temp->no;
        char *expr_str = wp_temp->expr;
        word_t val_old = wp_temp->val;
        bool success = false;
        word_t val_new = handleSDBExpr(expr_str, NULL, &success);
        if (val_new != val_old) {
            count++;
            if (count > 1) {
                LOG_BRIEF();
            }
            LOG_BRIEF("[sdb] [watch] hardware watchpoint %d: %s", no, expr_str);
            LOG_BRIEF("[sdb] [watch] value Old = %lu", val_old);
            LOG_BRIEF("[sdb] [watch] value New = %lu", val_new);
            wp_temp->val = val_new;
        }
        wp_temp = wp_temp->next;
    }

    return count;
}
