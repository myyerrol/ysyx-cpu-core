module fsm_top(
    input wire [0:0] i_clk,
    input wire [0:0] i_clr_n,
    input wire [0:0] i_ps2_clk,
    input wire [0:0] i_ps2_data,
    input wire [0:0] i_nextdata_n,
    output reg [0:0] o_ps2_ready,
    output reg [0:0] o_ps2_overflow,
    output reg [6:0] o_seg0,
    output reg [6:0] o_seg1,
    output reg [6:0] o_seg2,
    output reg [6:0] o_seg3,
    output reg [6:0] o_seg4,
    output reg [6:0] o_seg5
);

    reg [7:0] t_ps2_data    = 8'b0;
    reg [7:0] t_ps2_count   = 8'b0;
    // reg [0:0] t_ps2_release = 1'b0;

    ps2_keyboard ps2_keyboard_inst0(
        .i_clk(i_clk),
        .i_clr_n(i_clr_n),
        .i_ps2_clk(i_ps2_clk),
        .i_ps2_data(i_ps2_data),
        .i_nextdata_n(i_nextdata_n),
        .o_ps2_data(t_ps2_data),
        .o_ps2_ready(o_ps2_ready),
        .o_ps2_overflow(o_ps2_overflow),
        .o_ps2_count(t_ps2_count));

    seg seg_inst0(
        .i_num(t_ps2_data[3:0]),
        .o_seg(o_seg0));
    seg seg_inst1(
        .i_num(t_ps2_data[7:4]),
        .o_seg(o_seg1));

    reg [7:0] t_ps2_ascii = 8'b0;
    ps2_keyboard_lut ps2_keyboard_lut_inst0(
        .i_num(t_ps2_data),
        .o_num(t_ps2_ascii));
    seg seg_inst2(
        .i_num(t_ps2_ascii[3:0]),
        .o_seg(o_seg2));
    seg seg_inst3(
        .i_num(t_ps2_ascii[7:4]),
        .o_seg(o_seg3));

    reg [11:0] t_bcd = 12'b0;
    bcd bcd_inst0(
        .i_bin(t_ps2_count),
        .o_bcd(t_bcd));
    seg seg_inst4(
        .i_num(t_bcd[3:0]),
        .o_seg(o_seg4));
    seg seg_inst5(
        .i_num(t_bcd[7:4]),
        .o_seg(o_seg5));

endmodule
