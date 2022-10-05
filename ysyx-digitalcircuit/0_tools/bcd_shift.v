module bcd_shift(
    input  wire [19:0] i_num,
    output wire [19:0] o_num
);

    wire [3:0] reg1;
    wire [3:0] reg2;
    wire [3:0] reg3;

    // 左移大4加3比较
    bcd_cmp bcd_cmp_inst0(
        .i_cmp(i_num[19:16]),
        .o_cmp(reg1));
    // 左移大4加3比较
    bcd_cmp bcd_cmp_inst1(
        .i_cmp(i_num[15:12]),
        .o_cmp(reg2));
    // 左移大4加3比较
    bcd_cmp bcd_cmp_inst2(
        .i_cmp(i_num[11:8]),
        .o_cmp(reg3));
    // 比较完成，左移1位
    assign o_num = { reg1[2:0], reg2, reg3, i_num[7:0], 1'b0 };

endmodule
