module char_top(
    input  wire [0:0] i_clk,
    input  wire [0:0] i_rst,
    input  wire [0:0] i_ps2_clk,
    input  wire [0:0] i_ps2_data,
    output wire [0:0] o_vga_clk,
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

    char_ctr char_ctr_inst0(
        .i_h_addr(t_h_addr),
        .i_v_addr(t_v_addr)
    );

    reg [7:0] t_ps2_data     = 8'b0;
    reg [0:0] t_ps2_ready    = 1'b0;
    reg [0:0] t_ps2_overflow = 1'b0;
    reg [7:0] t_ps2_count    = 8'b0;
    ps2_keyboard ps2_keyboard_inst0(
        .i_clk(i_clk),
        .i_clr_n(~i_rst),
        .i_ps2_clk(i_ps2_clk),
        .i_ps2_data(i_ps2_data),
        .i_nextdata_n(1'b0),
        .o_ps2_data(t_ps2_data),
        .o_ps2_ready(t_ps2_ready),
        .o_ps2_overflow(t_ps2_overflow),
        .o_ps2_count(t_ps2_count));

    reg [7:0] t_ps2_ascii = 8'b0;
    ps2_keyboard_lut ps2_keyboard_lut_inst0(
        .i_num(t_ps2_data),
        .o_num(t_ps2_ascii));

endmodule
