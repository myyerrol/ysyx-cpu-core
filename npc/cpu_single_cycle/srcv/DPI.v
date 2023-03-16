import "DPI-C" function void judgeIsEbreak(input int flag);

module DPI(
    input wire[31:0] i_ebreak_flag
);

always @(*) begin
    judgeIsEbreak(i_ebreak_flag);
end

endmodule
