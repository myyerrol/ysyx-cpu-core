module shifter_top(
    input  wire [0:0] i_clk,
    output reg  [7:0] o_num,
    output reg  [6:0] o_seg0,
    output reg  [6:0] o_seg1
);

    reg [11:0] t_bcd = 12'b0000_0000_0000;

    shifter shifter_inst0(
        .i_clk(i_clk),
        .i_num(8'b0000_0001),
        .o_num(o_num));

    seg seg_inst0(
        .i_num(o_num[3:0]),
        .o_seg(o_seg0));

    seg seg_inst1(
        .i_num(o_num[7:4]),
        .o_seg(o_seg1));

endmodule
