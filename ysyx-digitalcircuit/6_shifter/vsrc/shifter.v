module shifter(
    input  wire [0:0] i_clk,
    input  wire [7:0] i_num,
    output reg  [7:0] o_num
);
    parameter CLK_NUM = 5000000;

    reg [31:0] t_count = 32'h00;
    reg [0:0]  t_bit   = 1'b0;

    initial begin
        o_num = i_num;
    end

    assign t_bit = o_num[4] ^ o_num[3] ^ o_num[2] ^ o_num[0];

    always @(posedge i_clk) begin
        if (t_count == CLK_NUM) begin
            t_count <= 32'h00;
            o_num <= { t_bit, o_num[7:1] };
        end
        else begin
            t_count <= t_count + 1;
        end
    end

endmodule
