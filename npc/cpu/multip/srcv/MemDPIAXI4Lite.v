`include "Config.v"

module MemDPIAXI4LiteLFU(
    input  wire                       iClock,
    input  wire                       iReset,

    input  wire                       bIFUAXISlaveARIO_arvalid,
    input  wire [`DATA_WIDTH - 1 : 0] bIFUAXISlaveARIO_araddr,
    output reg                        bIFUAXISlaveARIO_arready,

    input  wire                       bIFUAXISlaveRIO_rready,
    output reg                        bIFUAXISlaveRIO_rvalid,
    output wire [`INST_WIDTH - 1 : 0 ] bIFUAXISlaveRIO_rdata,
    output wire  [1 : 0]               bIFUAXISlaveRIO_rresp
);

    import "DPI-C" context function longint unsigned readInsDataByAXI4Lite(
        input longint unsigned addr,
        input byte unsigned len);

    initial begin
        $monitor("[btrace] arvalid: %d, araddr: 0x%x, arready: %d, rready: %d, rvalid: %d, rdata: 0x%x, rresp: %d",
                 bIFUAXISlaveARIO_arvalid,
                 bIFUAXISlaveARIO_araddr,
                 bIFUAXISlaveARIO_arready,
                 bIFUAXISlaveRIO_rready,
                 bIFUAXISlaveRIO_rvalid,
                 bIFUAXISlaveRIO_rdata,
                 bIFUAXISlaveRIO_rresp);
    end

    assign bIFUAXISlaveARIO_arready = 1'b1;

    always @(*) begin
        if (bIFUAXISlaveARIO_arvalid && bIFUAXISlaveARIO_arready) begin
            bIFUAXISlaveRIO_rvalid = 1'b1;
        end
        else begin
            bIFUAXISlaveRIO_rvalid = 1'b0;
        end
    end

    always @(bIFUAXISlaveRIO_rvalid) begin
        if (bIFUAXISlaveRIO_rvalid && bIFUAXISlaveRIO_rready) begin
            bIFUAXISlaveRIO_rdata = readInsDataByAXI4Lite(
                bIFUAXISlaveARIO_araddr,
                4);
            if (bIFUAXISlaveRIO_rdata != 32'b0) begin
                bIFUAXISlaveRIO_rresp = `AXI4_RRESP_OKEY;
            end
            else begin
                bIFUAXISlaveRIO_rresp = `AXI4_RRESP_SLVEER;
            end
        end
        else begin
            bIFUAXISlaveRIO_rdata = 32'b0;
            bIFUAXISlaveRIO_rresp = `AXI4_RRESP_OKEY;
        end
    end

endmodule
