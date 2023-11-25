module DPI(
    input  wire       iClock,
    input  wire       iReset,
    input  wire[ 7:0] iEbreakFlag,
    input  wire       iMemRdEn,
    input  wire[63:0] iMemRdAddrInst,
    input  wire[63:0] iMemRdAddrLoad,
    input  wire       iMemWrEn,
    input  wire[63:0] iMemWrAddr,
    input  wire[63:0] iMemWrData,
    input  wire[ 7:0] iMemWrLen,

    output  reg[31:0] oMemRdDataInst,
    output  reg[63:0] oMemRdDataLoad
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

always @(iEbreakFlag) begin
    judgeIsEbreak(iEbreakFlag);
end

always @(iMemRdAddrInst) begin
    if (iMemRdEn) begin
        oMemRdDataInst = readInsData(iMemRdAddrInst, 4);
    end
end

always @(iMemRdAddrLoad) begin
    if (iMemRdEn) begin
        oMemRdDataLoad = readMemData(iMemRdAddrLoad, 8);
    end
end

always @(iMemWrAddr or iMemWrData) begin
    if (iMemWrEn) begin
        writeMemData(iMemWrAddr, iMemWrData, iMemWrLen);
    end
end

endmodule
