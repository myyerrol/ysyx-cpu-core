`include "Config.v"

module AXI4LiteM(
    input  wire                       iClock,
    input  wire                       iReset,
    input  wire [`ADDR_WIDTH - 1 : 0] iAddr,
    input  wire [`DATA_WIDTH - 1 : 0] iData,
    input  wire [`MASK_WIDTH - 1 : 0] iMask,

    input  wire                       pAXI4M_ar_ready,
    output wire                       pAXI4M_ar_valid,
    output wire [`ADDR_WIDTH - 1 : 0] pAXI4M_ar_bits_addr,

    input  wire                       pAXI4M_r_valid,
    input  wire [`DATA_WIDTH - 1 : 0] pAXI4M_r_bits_data,
    input  wire [`RESP_WIDTH - 1 : 0] pAXI4M_r_bits_resp,
    output wire                       pAXI4M_r_ready,

    input  wire                       pAXI4M_aw_ready,
    output wire                       pAXI4M_aw_valid,
    output wire [`ADDR_WIDTH - 1 : 0] pAXI4M_aw_bits_addr,

    input  wire                       pAXI4M_w_ready,
    output wire                       pAXI4M_w_valid,
    output wire [`DATA_WIDTH - 1 : 0] pAXI4M_w_bits_data,
    output wire [`MASK_WIDTH - 1 : 0] pAXI4M_w_bits_strb,

    input  wire                       pAXI4M_b_valid,
    input  wire [`RESP_WIDTH - 1 : 0] pAXI4M_b_bits_resp,
    output wire                       pAXI4M_b_ready
);

    //-------------------------------------------------------------------------
    parameter STATE_IDLE = 3'b000;
    parameter STATE_AR   = 3'b001;
    parameter STATE_R    = 3'b010;
    parameter STATE_AW   = 3'b011;
    parameter STATE_W    = 3'b100;
    parameter STATE_B    = 3'b101;

    reg [2 : 0] r_state_curr;
    reg [2 : 0] r_state_next;

    //-------------------------------------------------------------------------
    reg                       r_arvalid;
    reg [`ADDR_WIDTH - 1 : 0] r_araddr;
    reg                       r_arstart;

    reg                       r_rready;

    reg                       r_awvalid;
    reg [`ADDR_WIDTH - 1 : 0] r_awaddr;
    reg                       r_awstart;

    reg                       r_wvalid;
    reg [`DATA_WIDTH - 1 : 0] r_wdata;
    reg [`MASK_WIDTH - 1 : 0] r_wstrb;

    reg                       r_bready;

    //-------------------------------------------------------------------------
    assign pAXI4M_ar_valid     = r_arvalid;
    assign pAXI4M_ar_bits_addr = r_araddr;

    // assign pAXI4M_r_ready      = r_rready;
    assign pAXI4M_r_ready      = 1'b1;

    assign pAXI4M_aw_valid     = r_awvalid;
    assign pAXI4M_aw_bits_addr = r_awaddr;

    assign pAXI4M_w_valid      = r_wvalid;
    assign pAXI4M_w_bits_data  = r_wdata;
    assign pAXI4M_w_bits_strb  = r_wstrb;

    // assign pAXI4M_b_ready      = r_bready;
    assign pAXI4M_b_ready      = 1'b1;

    //-------------------------------------------------------------------------
    always @(posedge iClock) begin
        if (!iReset) begin
            r_arvalid <= 1'b0;
        end
        else if (r_arstart) begin
            r_arvalid <= 1'b1;
        end
        else if (pAXI4M_ar_valid && pAXI4M_ar_ready) begin
            r_arvalid <= 1'b0;
        end
        else begin
            r_arvalid <= r_arvalid;
        end
    end

    always @(posedge iClock) begin
        if (!iReset) begin
            r_araddr <= `ADDR_WIDTH'b0;
        end
        else if (r_arstart) begin
            r_araddr <= iAddr;
        end
        else begin
            r_araddr <= r_araddr;
        end
    end

    // always @(posedge iClock) begin
    //     if (!iReset) begin
    //         r_rready <= 1'b0;
    //     end
    //     else if (pAXI4M_r_valid && pAXI4M_r_ready) begin
    //         r_rready <= 1'b1;
    //     end
    //     else begin
    //         r_rready <= r_rready;
    //     end
    // end

    always @(posedge iClock) begin
        if (!iReset) begin
            r_awvalid <= 1'b0;
        end
        else if (r_awstart) begin
            r_awvalid <= 1'b1;
        end
        else if (pAXI4M_aw_valid && pAXI4M_aw_ready) begin
            r_awvalid <= 1'b0;
        end
        else begin
            r_awvalid <= r_awvalid;
        end
    end

    always @(posedge iClock) begin
        if (!iReset) begin
            r_awaddr <= `DATA_WIDTH'b0;
        end
        else if (r_awstart) begin
            r_awaddr <= iAddr;
        end
        else begin
            r_awaddr <= r_awaddr;
        end
    end

    always @(posedge iClock) begin
        if (!iReset) begin
            r_wvalid <= 1'b0;
        end
        else if (pAXI4M_aw_valid && pAXI4M_aw_ready) begin
            r_wvalid <= 1'b1;
        end
        else begin
            r_wvalid <= r_wvalid;
        end
    end

    always @(posedge iClock) begin
        if (!iReset) begin
            r_wdata <= `DATA_WIDTH'b0;
        end
        else if (pAXI4M_w_valid && pAXI4M_w_ready) begin
            r_wdata <= iData;
        end
        else begin
            r_wdata <= iData;
        end
    end

    always @(posedge iClock) begin
        if (!iReset) begin
            r_wstrb <= `MASK_WIDTH'b0;
        end
        else if (pAXI4M_w_valid && pAXI4M_w_ready) begin
            r_wstrb <= iMask;
        end
        else begin
            r_wstrb <= r_wstrb;
        end
    end

    // always @(posedge iClock) begin
    //     if (!iReset) begin
    //         r_bready <= 1'b0;
    //     end
    //     else if (pAXI4M_w_valid && pAXI4M_w_ready) begin
    //         r_bready <= 1'b1;
    //     end
    //     else begin
    //         r_bready <= r_bready;
    //     end
    // end

    //-------------------------------------------------------------------------
    always @(posedge iClock) begin
        if (!iReset) begin
            r_state_curr <= STATE_IDLE;
        end
        else begin
            r_state_curr <= r_state_next;
        end
    end

    always @(*) begin
        case (r_state_curr)
            STATE_IDLE: begin
                if (r_arstart) begin
                    r_state_next = STATE_AR;
                end
                else if (r_awstart) begin
                    r_state_next = STATE_AW;
                end
                else begin
                    r_state_next = STATE_IDLE;
                end
            end
            STATE_AR: begin
                if (pAXI4M_ar_valid && pAXI4M_ar_ready) begin
                    r_state_next = STATE_R;
                end
                else begin
                    r_state_next = STATE_AR;
                end
            end
            STATE_R: begin
                if (pAXI4M_r_valid && pAXI4M_r_ready) begin
                    r_state_next = STATE_IDLE;
                end
                else begin
                    r_state_next = STATE_R;
                end
            end
            STATE_AW: begin
                if (pAXI4M_aw_valid && pAXI4M_aw_ready) begin
                    r_state_next = STATE_W;
                end
                else begin
                    r_state_next = STATE_AW;
                end
            end
            STATE_W: begin
                if (pAXI4M_w_valid && pAXI4M_w_ready) begin
                    r_state_next = STATE_B;
                end
                else begin
                    r_state_next = STATE_W;
                end
            end
            STATE_B: begin
                if (pAXI4M_b_valid && pAXI4M_b_ready) begin
                    r_state_next = STATE_IDLE;
                end
                else begin
                    r_state_next = STATE_B;
                end
            end
            default: begin
                r_state_next = STATE_IDLE;
            end
        endcase
    end

    always @(posedge iClock) begin
        if (!iReset) begin
        end
        else begin
            case (r_state_curr)
                STATE_IDLE: begin
                end
                STATE_AR: begin
                end
                STATE_R: begin
                end
                STATE_AW: begin
                end
                STATE_W: begin
                end
                STATE_B: begin
                end
                default: begin
                end
            endcase
        end
    end

endmodule


// module MemDPIAXI4LiteLFU(
//     input  wire                       iClock,
//     input  wire                       iReset,

//     input  wire                       bIFUAXISlaveARIO_arvalid,
//     input  wire [`DATA_WIDTH - 1 : 0] bIFUAXISlaveARIO_araddr,
//     output reg                        bIFUAXISlaveARIO_arready,

//     input  wire                       bIFUAXISlaveRIO_rready,
//     output reg                        bIFUAXISlaveRIO_rvalid,
//     output wire [`INST_WIDTH - 1 : 0 ] bIFUAXISlaveRIO_rdata,
//     output wire  [1 : 0]               bIFUAXISlaveRIO_rresp
// );

//     import "DPI-C" context function longint unsigned readInsDataByAXI4Lite(
//         input longint unsigned addr,
//         input byte unsigned len);

//     initial begin
//         $monitor("[btrace] arvalid: %d, araddr: 0x%x, arready: %d, rready: %d, rvalid: %d, rdata: 0x%x, rresp: %d",
//                  bIFUAXISlaveARIO_arvalid,
//                  bIFUAXISlaveARIO_araddr,
//                  bIFUAXISlaveARIO_arready,
//                  bIFUAXISlaveRIO_rready,
//                  bIFUAXISlaveRIO_rvalid,
//                  bIFUAXISlaveRIO_rdata,
//                  bIFUAXISlaveRIO_rresp);
//     end

//     assign bIFUAXISlaveARIO_arready = 1'b1;

//     always @(*) begin
//         if (bIFUAXISlaveARIO_arvalid && bIFUAXISlaveARIO_arready) begin
//             bIFUAXISlaveRIO_rvalid = 1'b1;
//         end
//         else begin
//             bIFUAXISlaveRIO_rvalid = 1'b0;
//         end
//     end

//     always @(bIFUAXISlaveRIO_rvalid) begin
//         if (bIFUAXISlaveRIO_rvalid && bIFUAXISlaveRIO_rready) begin
//             bIFUAXISlaveRIO_rdata = readInsDataByAXI4Lite(
//                 bIFUAXISlaveARIO_araddr,
//                 4);
//             if (bIFUAXISlaveRIO_rdata != 32'b0) begin
//                 bIFUAXISlaveRIO_rresp = `AXI4_RRESP_OKEY;
//             end
//             else begin
//                 bIFUAXISlaveRIO_rresp = `AXI4_RRESP_SLVEER;
//             end
//         end
//         else begin
//             bIFUAXISlaveRIO_rdata = 32'b0;
//             bIFUAXISlaveRIO_rresp = `AXI4_RRESP_OKEY;
//         end
//     end

// endmodule
