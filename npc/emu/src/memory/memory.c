#include <isa/isa.h>
#include <memory/host.h>
#include <memory/memory.h>

static uint8_t pmem[CONFIG_MSIZE] PG_ALIGN = {};

static void printfOutOfBoundInfo(paddr_t addr) {
    PANIC("[memory] address = " FMT_PADDR " is out of bound of pmem [" FMT_PADDR ", " \
          FMT_PADDR "] at pc = " FMT_WORD,
          addr,
          PMEM_LEFT,
          PMEM_RIGHT,
          cpu.pc);
}

uint8_t *convertGuestToHost(paddr_t paddr) {
    return pmem + paddr - CONFIG_MBASE;
}

paddr_t  convertHostToGuest(uint8_t *haddr) {
    return haddr - pmem + CONFIG_MBASE;
}

bool judgeAddrIsInPhyMem(paddr_t addr) {
    return addr - CONFIG_MBASE < CONFIG_MSIZE;
}

word_t readPhyMemData(paddr_t addr, int len) {
    if (likely(judgeAddrIsInPhyMem(addr))) {
        return readMemoryHost(convertGuestToHost(addr), len);
    }
    else {
        printfOutOfBoundInfo(addr);
        return 0;
    }
}

void writePhyMemData(paddr_t addr, int len, word_t data) {
    if (likely(judgeAddrIsInPhyMem(addr))) {
        writeMemoryHost(convertGuestToHost(addr), len, data);
        return;
    }
    else {
        printfOutOfBoundInfo(addr);
    }
}

void genMemFile(const char *mem_file, int size) {
    if (mem_file != NULL) {
        FILE *fp = fopen(mem_file, "w");
        ASSERT(fp, "[memory] can not open '%s'", mem_file);
        uint32_t *pmem_p = (uint32_t *)pmem;
        for (int i = 0; i < size; i++) {
            fprintf(fp, FMT_PADDR "\n", pmem_p[i]);
        }
        fclose(fp);
    }
}

void initMem() {
#ifdef CONFIG_MEM_RANDOM
    uint32_t *pmem_p = (uint32_t *)pmem;
    for (int i = 0; i < (int)(CONFIG_MSIZE / sizeof(pmem_p[0])); i++) {
        pmem_p[i] = rand();
    }
#endif
    LOG_BRIEF_COLOR("[memory] physical area: [" FMT_PADDR ", " FMT_PADDR "]",
        PMEM_LEFT,
        PMEM_RIGHT);
}

void printfMemData(int size) {
    uint32_t *pmem_p = (uint32_t *)pmem;
    for (int i = 0; i < size; i++) {
        LOG_BRIEF_COLOR("[memory] physical data[%05d]: " FMT_PADDR,
                                                         i,
                                                         pmem_p[i]);
    }
}
