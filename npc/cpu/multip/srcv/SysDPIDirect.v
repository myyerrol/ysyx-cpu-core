`include "Config.v"

module SysDPIDirect(
    input  wire                      iClock,
    input  wire                      iReset,
    input  wire[`BYTE_WIDTH - 1 : 0] iEbreakFlag
);

import "DPI-C" context function void judgeIsEbreak(input byte unsigned flag);

always @(iEbreakFlag) begin
    judgeIsEbreak(iEbreakFlag);
end

endmodule
