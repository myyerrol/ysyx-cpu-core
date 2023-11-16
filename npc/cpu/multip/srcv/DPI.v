module DPI(
    input wire[07:0] iEbreakFlag,
    input wire[63:0] iMemRdAddrInst,
    input wire[63:0] iMemRdAddrLoad,
    input wire[00:0] iMemWrEn,
    input wire[63:0] iMemWrAddr,
    input wire[63:0] iMemWrData,
    input wire[07:0] iMemWrLen,
    input wire[63:0] iRegData,

    output reg[63:0] oMemRdDataInst,
    output reg[63:0] oMemRdDataLoad,
    output reg[63:0] oRegAddr
);

import "DPI-C" context function void judgeIsEbreak(input byte unsigned flag);
import "DPI-C" context function longint unsigned readInsData(
    input longint unsigned addr,
    input byte unsigned len);
import "DPI-C" context function longint unsigned readMemData(
    input longint unsigned addr,
    input byte unsigned len);
import "DPI-C" context function void writeMemData(input longint unsigned addr,
                                                  input longint unsigned data,
                                                  input byte unsigned len);

export "DPI-C" function getISARegData;

function longint unsigned getISARegData(longint unsigned addr);
    oRegAddr = addr;
    return iRegData;
endfunction

always @(iEbreakFlag) begin
    judgeIsEbreak(iEbreakFlag);
end

always @(iMemRdAddrInst) begin
    oMemRdDataInst = readInsData(iMemRdAddrInst, 8);
end

always @(iMemRdAddrLoad) begin
    oMemRdDataLoad = readMemData(iMemRdAddrLoad, 8);
end

always @(iMemWrAddr or iMemWrData) begin
    if (iMemWrEn) begin
        writeMemData(iMemWrAddr, iMemWrData, iMemWrLen);
    end
end

endmodule
