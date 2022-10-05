module counter_bcd_cmp(
    input  wire [3:0] i_cmp,
    output reg  [3:0] o_cmp
);

    always @(*) begin
        // 大于4加3
        if (i_cmp > 4) begin
            o_cmp = i_cmp + 3;
        end
        // 小于等于4不处理
        else begin
            o_cmp = i_cmp;
        end
    end

endmodule
