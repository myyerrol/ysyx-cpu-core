module DPI(
    input  wire[31:0] iEbreakFlag,
    input  wire[63:0] iMemRdAddrInst,
    input  wire[63:0] iMemRdAddrLoad,
    input  wire[0: 0] iMemWrEn,
    input  wire[63:0] iMemWrAddr,
    input  wire[63:0] iMemWrData,
    output  reg[63:0] oMemRdDataInst,
    output  reg[63:0] oMemRdDataLoad
    // input  wire[63:0] iRegVal,
    // output wire[63:0] oRegAddr
);

import "DPI-C" function void judgeIsEbreak(input int flag);
import "DPI-C" function longint readMemData(input longint unsigned addr);
import "DPI-C" function void writeMemData(input longint unsigned addr , input longint unsigned data, input byte len);

// export "DPI-C" function setRegAddr;

// function void setRegAddr(int addr);
//     $display("Hello");
// endfunction

always @(iEbreakFlag) begin
    judgeIsEbreak(iEbreakFlag);
    $display("iEbreakFlag: %d\n", iEbreakFlag);
end

always @(iMemRdAddrInst) begin
    oMemRdDataInst = readMemData(iMemRdAddrInst);
end

always @(iMemRdAddrLoad) begin
    oMemRdDataLoad = readMemData(iMemRdAddrLoad);
end

always @(iMemWrAddr) begin
    if (iMemWrEn == 1'b1) begin
        writeMemData(iMemWrAddr, iMemWrData, 8);
    end
end

endmodule
