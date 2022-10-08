module fsm_top(
    input wire [0:0] i_clk,
    input wire [0:0] i_clr_n,
    input wire [0:0] i_ps2_clk,
    input wire [0:0] i_ps2_data,
    input wire [0:0] i_nextdata_n,

    output reg [6:0] o_seg0,
    output reg [6:0] o_seg1,
    output reg [6:0] o_seg2,
    output reg [6:0] o_seg3,
    output reg [6:0] o_seg4,
    output reg [6:0] o_seg5
);

    reg [7:0] t_data = 8'h00;
    reg [0:0] t_ready = 1'h0;
    reg [0:0] t_overflow = 1'h0;

    ps2_keyboard ps2_keyboard_inst0(
        .i_clk(i_clk),
        .i_clr_n(i_clr_n),
        .i_ps2_clk(i_ps2_clk),
        .i_ps2_data(i_ps2_data),
        .i_nextdata_n(i_nextdata_n),
        .o_data(t_data),
        .o_ready(t_ready),
        .o_overflow(t_overflow));

    seg seg_inst0(
        .i_num(t_data[3:0]),
        .o_seg(seg0));

    seg seg_inst1(
        .i_num(t_data[7:4]),
        .o_seg(seg1));

    reg [7:0] t_ascii = 8'h00;

    ps2_keyboard_lut ps2_keyboard_lut_inst0(
        .i_num(t_data),
        .o_num(t_ascii));

    seg seg_inst2(
        .i_num(t_ascii[3:0]),
        .o_seg(seg2));

    seg seg_inst3(
        .i_num(t_ascii[7:4]),
        .o_seg(seg3));

endmodule
