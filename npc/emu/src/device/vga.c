#include <device/vga.h>
#include <memory/memory.h>

#define SCREEN_W (MUXDEF(CONFIG_VGA_SIZE_800x600, 800, 400))
#define SCREEN_H (MUXDEF(CONFIG_VGA_SIZE_800x600, 600, 300))

uint32_t getDeviceVGAScreenWidth() {
    return SCREEN_W;
}

uint32_t getDeviceVGAScreenHeight() {
    return SCREEN_H;
}

uint32_t getDeviceVGAScreenSize() {
    return getDeviceVGAScreenWidth() * getDeviceVGAScreenHeight() *
           sizeof(uint32_t);
}

static uint64_t  vgactl;
static uint32_t *vmem[SCREEN_W * SCREEN_H];

#ifdef CONFIG_VGA_SHOW_SCREEN
#include <SDL2/SDL.h>

static SDL_Renderer *renderer = NULL;
static SDL_Texture  *texture  = NULL;

static void initDeviceVGAScreen() {
    SDL_Window *window = NULL;
    char title[128];
    sprintf(title, "%s-NPC", str(__GUEST_ISA__));
    SDL_Init(SDL_INIT_VIDEO);
    SDL_CreateWindowAndRenderer(
        SCREEN_W * (MUXDEF(CONFIG_VGA_SIZE_400x300, 2, 1)),
        SCREEN_H * (MUXDEF(CONFIG_VGA_SIZE_400x300, 2, 1)),
        0,
        &window,
        &renderer);
    SDL_SetWindowTitle(window, title);
    texture = SDL_CreateTexture(
        renderer,
        SDL_PIXELFORMAT_ARGB8888,
        SDL_TEXTUREACCESS_STATIC,
        SCREEN_W,
        SCREEN_H);
}

static void updateDeviceVGAScreenStep() {
    SDL_UpdateTexture(texture, NULL, vmem, SCREEN_W * sizeof(uint32_t));
    SDL_RenderClear(renderer);
    SDL_RenderCopy(renderer, texture, NULL, NULL);
    SDL_RenderPresent(renderer);
}
#endif

void initDeviceVGA() {
    IFDEF(CONFIG_VGA_SHOW_SCREEN, initDeviceVGAScreen());
    IFDEF(CONFIG_VGA_SHOW_SCREEN, memset(vmem, 0, getDeviceVGAScreenSize()));
}

void updateDeviceVGAScreen() {
#ifdef CONFIG_VGA_SHOW_SCREEN
    // if (readPhyMemData(CONFIG_VGA_CTL_MMIO + 4, 4) != 0) {
    //     updateDeviceVGAScreenStep();
    //     writePhyMemData(CONFIG_VGA_CTL_MMIO + 4, 4, 0);
    // }
#endif
}
