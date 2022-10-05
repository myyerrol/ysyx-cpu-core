module regs_top(
    input  wire [0:0] i_clk,
    input  wire [0:0] i_wt_en,
    input  wire [7:0] i_data,
    input  wire [3:0] i_data_addr,
    input  wire [3:0] o_data_addr,
    output reg  [7:0] o_data
);

    regs regs_inst0(
        .i_clk(i_clk),
        .i_wt_en(i_wt_en),
        .i_data(i_data),
        .i_data_addr(i_data_addr),
        .o_data_addr(o_data_addr),
        .o_data(o_data));
    defparam regs_inst0.REGS_WIDTH = 8;
    defparam regs_inst0.REGS_WIDTH_ADDR = 4;

endmodule
