module encoder_top (
    input  wire [0:0] i_en,
    input  wire [7:0] i_num,
    output reg  [3:0] o_num,
    output reg  [6:0] o_seg
);

    encoder_8to4 encoder_8to4_inst0(
        .i_en(i_en),
        .i_num(i_num),
        .o_num(o_num)
    );

    seg seg_inst0(
        .i_num({ 1'b0, o_num[2:0]}),
        .o_seg(o_seg)
    );

endmodule
