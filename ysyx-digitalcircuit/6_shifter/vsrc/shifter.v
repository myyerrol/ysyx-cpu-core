module shifter(
    input  wire [0:0] i_clk,
    input  wire [7:0] i_num,
    output reg  [7:0] o_num
);

    reg [0:0] t_bit = 1'b0;

    initial begin
        o_num = i_num;
    end

    assign t_bit = o_num[4] ^ o_num[3] ^ o_num[2] ^ o_num[0];

    always @(posedge i_clk) begin
        o_num <= { t_bit, o_num[7:1] };
    end

endmodule
