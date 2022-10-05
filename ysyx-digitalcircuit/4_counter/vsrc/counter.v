module counter(
    input  wire [0:0] i_clk,
    input  wire [0:0] i_rst,
    input  wire [2:0] i_ctr,
    output reg  [7:0] o_num
);

    parameter CLK_NUM = 5000000;
    parameter SEG_NUM = 99;

    reg [31:0] t_count = 32'h00;
    reg [0:0]  t_zero  = 1'b1;
    reg [0:0]  t_start = 1'b1;

    always @(posedge i_clk) begin
        if (i_rst) begin
            t_count <= 32'h00;
            o_num <= 8'd0;
        end
        else begin
            if (i_ctr[0] == 1'b1) begin
                if (t_zero == 1'b1) begin
                    o_num <= 8'd0;
                    t_zero <= 1'b0;
                end
                else begin
                    ;
                end
            end
            else begin
                t_zero <= 1'b1;
            end

            if (i_ctr[1] == 1'b1) begin
                t_start <= 1'b0;
            end
            else begin
                ;
            end

            if (i_ctr[2] == 1'b1) begin
                t_start <= 1'b1;
            end
            else begin
                ;
            end

            if (t_start == 1'b1) begin
                if (t_count == CLK_NUM) begin
                    t_count <= 32'h00;
                    if (o_num == SEG_NUM) begin
                        o_num <= 8'd0;
                    end
                    else begin
                        o_num <= o_num +1;
                    end
                end
                else begin
                    t_count <= t_count + 1;
                end
            end
        end
    end

endmodule
