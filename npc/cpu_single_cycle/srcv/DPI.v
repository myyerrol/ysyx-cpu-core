import "DPI-C" function void judgeEbreak(input int flag);

module DPI(
    input wire[31:0] i_ebreak_flag
);

always @(*) begin
    judgeEbreak(i_ebreak_flag);
end

endmodule
