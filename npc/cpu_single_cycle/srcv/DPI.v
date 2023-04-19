import "DPI-C" function void judgeIsEbreak(input int flag);

module DPI(
    input wire[31:0] iEbreakFlag
);

always @(*) begin
    judgeIsEbreak(iEbreakFlag);
end

endmodule
