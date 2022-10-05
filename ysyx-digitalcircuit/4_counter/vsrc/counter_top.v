module counter_top(
    input  wire [0:0] i_clk,
    input  wire [0:0] i_rst,
    input  wire [2:0] i_ctr,
    output reg  [6:0] o_seg0,
    output reg  [6:0] o_seg1
);

    reg [7:0]  t_num = 8'b0000_0000;
    reg [11:0] t_bcd = 12'b0000_0000_0000;

    counter counter_inst0(
        .i_clk(i_clk),
        .i_rst(i_rst),
        .i_ctr(i_ctr),
        .o_num(t_num));

    bcd bcd_inst0(
        .i_bin(t_num),
        .o_bcd(t_bcd));

    seg seg_inst0(
        .i_num(t_bcd[3:0]),
        .o_seg(o_seg0));

    seg seg_inst1(
        .i_num(t_bcd[7:4]),
        .o_seg(o_seg1));

endmodule
