`include "Config.v"

module MemDPIDirectTime(
    input  wire                       iClock,
    input  wire                       iReset,
    input  wire                       iMemRdEn,
    input  wire [`ADDR_WIDTH - 1 : 0] iMemRdAddrInst,
    input  wire [`ADDR_WIDTH - 1 : 0] iMemRdAddrLoad,
    input  wire                       iMemWrEn,
    input  wire [`ADDR_WIDTH - 1 : 0] iMemWrAddr,
    input  wire [`DATA_WIDTH - 1 : 0] iMemWrData,
    input  wire [`BYTE_WIDTH - 1 : 0] iMemWrLen,

    output reg  [`INST_WIDTH - 1 : 0] oMemRdDataInst,
    output reg  [`DATA_WIDTH - 1 : 0] oMemRdDataLoad
);

    import "DPI-C" context function int unsigned readInsData(
        input longint unsigned addr,
        input byte unsigned len);
    import "DPI-C" context function longint unsigned readMemData(
        input longint unsigned addr,
        input byte unsigned len);
    import "DPI-C" context function void writeMemData(input longint unsigned addr,
                                                      input longint unsigned data,
                                                      input byte unsigned len);

    always @(posedge iClock) begin
        if (iReset) begin
            oMemRdDataInst <= `INST_WIDTH'b0;
        end
        else if (iMemRdEn) begin
            oMemRdDataInst <= readInsData(iMemRdAddrInst, 4);
        end
        else begin
            oMemRdDataInst <= oMemRdDataInst;
        end
    end

endmodule
