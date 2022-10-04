module encoder_8to4 (
    input  wire [0:0] i_en,
    input  wire [7:0] i_num,
    output reg  [3:0] o_num
);
    integer i = 0;

    always @(*) begin
        if (i_en) begin
            o_num = 4'b0000;
            for (i = 0; i <= 7; i++) begin
                if (i_num[i] == 1'b1) begin
                    o_num = { 1'b1, i[2:0] };
                end
            end
        end
        else begin
            o_num = 4'b0000;
        end
    end

endmodule
