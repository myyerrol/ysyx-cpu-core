module DPI(
    input  wire[31:0] iEbreakFlag,
    input  wire[63:0] iMemRdAddr,
    input  wire[0: 0] iMemWrEn,
    input  wire[63:0] iMemWrAddr,
    input  wire[63:0] iMemWrData,
    output  reg[63:0] oMemRdData
    // input  wire[63:0] iRegVal,
    // output wire[63:0] oRegAddr
);

import "DPI-C" function void judgeIsEbreak(input int flag);
import "DPI-C" function longint readMemData(input longint addr);
import "DPI-C" function void writeMemData(input longint addr, input longint data, input byte len);

// export "DPI-C" function setRegAddr;

// function void setRegAddr(int addr);
//     $display("Hello");
// endfunction

always @(iEbreakFlag) begin
    judgeIsEbreak(iEbreakFlag);
    // oMemRdData = readMemData(iMemRdAddr);
    // if (iMemWrEn == 1'b1) begin
    //     writeMemData(iMemWrAddr, iMemWrData, 8);
    // end
end

always @(iMemRdAddr) begin
    oMemRdData = readMemData(iMemRdAddr);
end

always @(iMemWrEn) begin
    if (iMemWrEn == 1'b1) begin
        writeMemData(iMemWrAddr, iMemWrData, 8);
    end
end

endmodule
