`include "Config.v"

module AXI4LiteS(
    input  wire                       iClock,
    input  wire                       iReset,
    input  wire [`MODE_WIDTH - 1 : 0] iMode,
    input  wire [`DATA_WIDTH - 1 : 0] iRdData,
    input  wire [`RESP_WIDTH - 1 : 0] iResp,
    output wire [`ADDR_WIDTH - 1 : 0] oRdAddr,
    output wire [`ADDR_WIDTH - 1 : 0] oWrAddr,
    output wire [`DATA_WIDTH - 1 : 0] oWrData,
    output wire [`MASK_WIDTH - 1 : 0] oWrMask,

    input  wire                       pAXI4_ar_valid,
    input  wire [`ADDR_WIDTH - 1 : 0] pAXI4_ar_bits_addr,
    output wire                       pAXI4_ar_ready,

    input  wire                       pAXI4_r_ready,
    output wire                       pAXI4_r_valid,
    output wire [`DATA_WIDTH - 1 : 0] pAXI4_r_bits_data,
    output wire [`RESP_WIDTH - 1 : 0] pAXI4_r_bits_resp,

    input  wire                       pAXI4_aw_valid,
    input  wire [`ADDR_WIDTH - 1 : 0] pAXI4_aw_bits_addr,
    output wire                       pAXI4_aw_ready,

    input  wire                       pAXI4_w_valid,
    input  wire [`DATA_WIDTH - 1 : 0] pAXI4_w_bits_data,
    input  wire [`MASK_WIDTH - 1 : 0] pAXI4_w_bits_strb,
    output wire                       pAXI4_w_ready,

    input  wire                       pAXI4_b_ready,
    output wire                       pAXI4_b_valid,
    output wire [`RESP_WIDTH - 1 : 0] pAXI4_b_bits_resp
);

    //-------------------------------------------------------------------------
    wire w_rd_addr_handshake;
    wire w_rd_data_handshake;
    wire w_wr_addr_handshake;
    wire w_wr_data_handshake;
    wire w_wr_resp_handshake;

    //-------------------------------------------------------------------------
    reg                       r_arready;
    reg                       r_rvalid;
    reg                       r_awready;
    reg                       r_wready;
    reg                       r_bvalid;

    reg [`ADDR_WIDTH - 1 : 0] r_addr;
    reg [`RESP_WIDTH - 1 : 0] r_rd_resp;
    reg [`RESP_WIDTH - 1 : 0] r_wr_resp;

    //-------------------------------------------------------------------------
    assign oRdAddr           = pAXI4_ar_bits_addr;
    assign oWrAddr           = pAXI4_aw_bits_addr;
    assign oWrData           = pAXI4_w_bits_data;
    assign oWrMask           = pAXI4_w_bits_strb;

    assign pAXI4_ar_ready    = r_arready;
    assign pAXI4_r_valid     = r_rvalid;
    assign pAXI4_r_bits_data = iRdData;
    // assign pAXI4_r_bits_resp = r_rd_resp;
    assign pAXI4_r_bits_resp = iResp;
    assign pAXI4_aw_ready    = r_awready;
    assign pAXI4_w_ready     = r_wready;
    assign pAXI4_b_valid     = r_bvalid;
    // assign pAXI4_b_bits_resp = r_wr_resp;
    assign pAXI4_b_bits_resp = iResp;

    assign w_rd_addr_handshake = pAXI4_ar_valid && pAXI4_ar_ready;
    assign w_rd_data_handshake = pAXI4_r_valid  && pAXI4_r_ready;
    assign w_wr_addr_handshake = pAXI4_aw_valid && pAXI4_aw_ready;
    assign w_wr_data_handshake = pAXI4_w_valid  && pAXI4_w_ready;
    assign w_wr_resp_handshake = pAXI4_b_valid  && pAXI4_b_ready;

    // always @(*) begin
    //     if (iMode === `MODE_RD) begin
    //         r_addr    = pAXI4_ar_bits_addr;
    //         r_rd_resp = iResp;
    //     end
    // end

    //-------------------------------------------------------------------------
    always @(posedge iClock) begin
        if (iReset) begin
            r_arready <= 1'b1;
        end
        else if (w_rd_addr_handshake) begin
            r_arready <= 1'b0;
        end
        else if (w_rd_data_handshake) begin
            r_arready <= 1'b1;
        end
        else begin
            r_arready <= r_arready;
        end
    end

    always @(posedge iClock) begin
        if (iReset) begin
            r_rvalid <= 1'b0;
        end
        else if (w_rd_addr_handshake) begin
            r_rvalid <= 1'b1;
        end
        else if (w_rd_data_handshake) begin
            r_rvalid <= 1'b0;
        end
        else begin
            r_rvalid <= r_rvalid;
        end
    end

    always @(posedge iClock) begin
        if (iReset) begin
            r_awready <= 1'b1;
        end
        else if (w_wr_addr_handshake) begin
            r_awready <= 1'b0;
        end
        else if (w_wr_data_handshake) begin
            r_awready <= 1'b1;
        end
        else begin
            r_awready <= r_awready;
        end
    end

    always @(posedge iClock) begin
        if (iReset) begin
            r_wready <= 1'b1;
        end
        else if (w_wr_data_handshake) begin
            r_wready <= 1'b0;
        end
        else if (w_wr_resp_handshake) begin
            r_wready <= 1'b1;
        end
        else begin
            r_wready <= r_awready;
        end
    end

    always @(posedge iClock) begin
        if (iReset) begin
            r_bvalid <= 1'b0;
        end
        else if (w_wr_resp_handshake) begin
            r_bvalid <= 1'b1;
        end
        else if (r_bvalid) begin
            r_bvalid <= 1'b0;
        end
        else begin
            r_bvalid <= r_bvalid;
        end
    end

endmodule
