module counter_bcd (
    input  wire [7:0]  i_bin,
    output wire [11:0] o_bcd
);

    wire [19:0] t_bcd_reg0;
    wire [19:0] t_bcd_reg1;
    wire [19:0] t_bcd_reg2;
    wire [19:0] t_bcd_reg3;
    wire [19:0] t_bcd_reg4;
    wire [19:0] t_bcd_reg5;
    wire [19:0] t_bcd_reg6;
    wire [19:0] t_bcd_reg7;
    wire [19:0] t_bcd_reg8;

    // 将输入的8位二进制转换成20位
    assign t_bcd_reg0 = { 12'b0000_0000_0000, i_bin };
    // 第1次移位
    counter_bcd_shift counter_bcd_shift_inst0(
        .i_num(t_bcd_reg0),
        .o_num(t_bcd_reg1));
    // 第2次移位
    counter_bcd_shift counter_bcd_shift_inst1(
        .i_num(t_bcd_reg1),
        .o_num(t_bcd_reg2));
    // 第3次移位
    counter_bcd_shift counter_bcd_shift_inst2(
        .i_num(t_bcd_reg2),
        .o_num(t_bcd_reg3));
    // 第4次移位
    counter_bcd_shift counter_bcd_shift_inst3(
        .i_num(t_bcd_reg3),
        .o_num(t_bcd_reg4));
    // 第5次移位
    counter_bcd_shift counter_bcd_shift_inst4(
        .i_num(t_bcd_reg4),
        .o_num(t_bcd_reg5));
    // 第6次移位
    counter_bcd_shift counter_bcd_shift_inst5(
        .i_num(t_bcd_reg5),
        .o_num(t_bcd_reg6));
    // 第7次移位
    counter_bcd_shift counter_bcd_shift_inst6(
        .i_num(t_bcd_reg6),
        .o_num(t_bcd_reg7));
    // 第8次移位
    counter_bcd_shift counter_bcd_shift_inst7(
        .i_num(t_bcd_reg7),
        .o_num(t_bcd_reg8));
    // 输出BCD
    assign o_bcd = { t_bcd_reg8[19:8] };

endmodule
