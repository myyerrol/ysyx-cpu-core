module vga_top(
    input  wire [0:0] i_clk,
    input  wire [0:0] i_rst,
    output wire [0:0] o_vga_hsync,
    output wire [0:0] o_vga_vsync,
    output wire [0:0] o_vga_blank_n,
    output wire [7:0] o_vga_r,
    output wire [7:0] o_vga_g,
    output wire [7:0] o_vga_b
);

    wire [23:0] t_vga_data;
    wire [9:0]  t_h_addr;
    wire [9:0]  t_v_addr;

    vga_ctrl vga_ctrl_inst0(
        .i_pclk(i_clk),
        .i_reset(i_rst),
        .i_vga_data(t_vga_data),
        .o_h_addr(t_h_addr),
        .o_v_addr(t_v_addr),
        .o_hsync(o_vga_hsync),
        .o_vsync(o_vga_vsync),
        .o_valid(o_vga_blank_n),
        .o_vga_r(o_vga_r),
        .o_vga_g(o_vga_g),
        .o_vga_b(o_vga_b)
    );

    vga_mem vga_mem_inst0(
        .i_h_addr(t_h_addr),
        .i_v_addr(t_v_addr[8:0]),
        .o_vga_data(t_vga_data)
    );

endmodule
