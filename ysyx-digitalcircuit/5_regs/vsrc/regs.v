module regs #(
    parameter REGS_WIDTH      = 8,
    parameter REGS_WIDTH_ADDR = 4
) (
    input  wire [0:0]                 i_clk,
    input  wire [0:0]                 i_wt_en,
    input  wire [REGS_WIDTH-1:0]      i_data,
    input  wire [REGS_WIDTH_ADDR-1:0] i_data_addr,
    input  wire [REGS_WIDTH_ADDR-1:0] o_data_addr,
    output reg  [REGS_WIDTH-1:0]      o_data
);

    reg [REGS_WIDTH-1:0] regs[(2**REGS_WIDTH_ADDR)-1:0];

    initial begin
        $readmemb("mem.txt", regs);
    end

    always @(posedge i_clk) begin
        if (i_wt_en) begin
            regs[i_data_addr] <= i_data;
        end
        else begin
            ;
        end
    end

    assign o_data = regs[o_data_addr];

endmodule
