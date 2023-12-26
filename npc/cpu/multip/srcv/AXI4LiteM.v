`include "Config.v"

module AXI4LiteM(
    input  wire                       iClock,
    input  wire                       iReset,

    input  wire [`MODE_WIDTH - 1 : 0] iMode,
    input  wire                       iRdValid,
    input  wire [`ADDR_WIDTH - 1 : 0] iRdAddr,
    input  wire                       iWrValid,
    input  wire [`ADDR_WIDTH - 1 : 0] iWrAddr,
    input  wire [`DATA_WIDTH - 1 : 0] iWrData,
    input  wire [`MASK_WIDTH - 1 : 0] iWrMask,
    output wire [`DATA_WIDTH - 1 : 0] oRdData,
    output wire [`RESP_WIDTH - 1 : 0] oRdResp,
    output wire [`RESP_WIDTH - 1 : 0] oWrResp,

    input  wire                       pAXI4_ar_ready,
    output wire                       pAXI4_ar_valid,
    output wire [`ADDR_WIDTH - 1 : 0] pAXI4_ar_bits_addr,

    input  wire                       pAXI4_r_valid,
    input  wire [`DATA_WIDTH - 1 : 0] pAXI4_r_bits_data,
    input  wire [`RESP_WIDTH - 1 : 0] pAXI4_r_bits_resp,
    output wire                       pAXI4_r_ready,

    input  wire                       pAXI4_aw_ready,
    output wire                       pAXI4_aw_valid,
    output wire [`ADDR_WIDTH - 1 : 0] pAXI4_aw_bits_addr,

    input  wire                       pAXI4_w_ready,
    output wire                       pAXI4_w_valid,
    output wire [`DATA_WIDTH - 1 : 0] pAXI4_w_bits_data,
    output wire [`MASK_WIDTH - 1 : 0] pAXI4_w_bits_strb,

    input  wire                       pAXI4_b_valid,
    input  wire [`RESP_WIDTH - 1 : 0] pAXI4_b_bits_resp,
    output wire                       pAXI4_b_ready
);

    always @(posedge iClock) begin
`ifdef VTRACE_MONITOR
        $display("[vtrace] clock:      %d, reset:      %d", iClock, iReset);
`ifdef VTRACE_MONITOR_AXI4_RD
        $display("[vtrace] ------------------------------------------------------");
        $display("[vtrace] state curr: %d, state next: %d", r_state_rd_curr,
                                                            r_state_rd_next);
        $display("[vtrace] arvalid:    %d, arready:    %d, araddr: %x",
                 pAXI4_ar_valid,
                 pAXI4_ar_ready,
                 pAXI4_ar_bits_addr);
        $display("[vtrace] rvalid:     %d, rready:     %d, rdata:  %x, rresp: %d",
                 pAXI4_r_valid,
                 pAXI4_r_ready,
                 pAXI4_r_bits_data,
                 pAXI4_r_bits_resp);
`endif
`ifdef VTRACE_MONITOR_AXI4_WR
        $display("[vtrace] ------------------------------------------------------");
        $display("[vtrace] state curr: %d, state next: %d", r_state_wr_curr,
                                                            r_state_wr_next);
        $display("[vtrace] awvalid:    %d, awready:    %d, awaddr: %x",
                 pAXI4_aw_valid,
                 pAXI4_aw_ready,
                 pAXI4_aw_bits_addr);
        $display("[vtrace] wvalid:     %d, wready:     %d, wdata:  %x, wstrb: %x",
                 pAXI4_w_valid,
                 pAXI4_w_ready,
                 pAXI4_w_bits_data,
                 pAXI4_w_bits_strb);
        $display("[vtrace] bvalid:     %d, bready:     %d, bresp:  %d",
                 pAXI4_b_valid,
                 pAXI4_b_ready,
                 pAXI4_b_bits_resp);
`endif
        $display();
`endif
    end

    //-------------------------------------------------------------------------
    parameter P_STATE_IDLE     = 'd0;
    parameter P_STATE_RD_ADDR  = 'd1;
    parameter P_STATE_RD_DATA  = 'd2;
    parameter P_STATE_RD_END   = 'd3;
    parameter P_STATE_WR_ADDR  = 'd1;
    parameter P_STATE_WR_DATA  = 'd2;
    parameter P_STATE_WR_END   = 'd3;

    reg [2 : 0] r_state_rd_curr;
    reg [2 : 0] r_state_rd_next;
    reg [2 : 0] r_state_wr_curr;
    reg [2 : 0] r_state_wr_next;

    //-------------------------------------------------------------------------
    wire                       w_awvalid;
    wire [`ADDR_WIDTH - 1 : 0] w_awaddr;
    wire [`DATA_WIDTH - 1 : 0] w_wdata;
    wire [`MASK_WIDTH - 1 : 0] w_wstrb;

    wire                       w_rd_start;
    wire                       w_rd_last;
    wire                       w_rd_addr_handshake;
    wire                       w_rd_data_handshake;
    wire                       w_wr_start;
    wire                       w_wr_last;
    wire                       w_wr_addr_handshake;
    wire                       w_wr_data_handshake;
    wire                       w_wr_resp_handshake;

    //-------------------------------------------------------------------------
    reg                       r_arvalid;
    reg [`ADDR_WIDTH - 1 : 0] r_araddr;
    reg                       r_rready;
    reg [`DATA_WIDTH - 1 : 0] r_rdata;
    reg                       r_awvalid;
    reg [`ADDR_WIDTH - 1 : 0] r_awaddr;
    reg                       r_wvalid;
    reg [`DATA_WIDTH - 1 : 0] r_wdata;
    reg [`MASK_WIDTH - 1 : 0] r_wstrb;
    reg                       r_bready;

    //-------------------------------------------------------------------------
    assign oRdData             = (iReset) ? `DATA_WIDTH'b0 : r_rdata;
    assign oRdResp             = `RESP_OKEY;
    assign oWrResp             = `RESP_OKEY;

    assign pAXI4_ar_valid      = r_arvalid;
    assign pAXI4_ar_bits_addr  = r_araddr;
    assign pAXI4_r_ready       = r_rready;
    assign pAXI4_aw_valid      = r_awvalid;
    assign pAXI4_aw_bits_addr  = r_awaddr;
    assign pAXI4_w_valid       = r_wvalid;
    assign pAXI4_w_bits_data   = w_wdata;
    assign pAXI4_w_bits_strb   = w_wstrb;
    assign pAXI4_b_ready       = 1'b1;

    assign w_awaddr            = (iReset) ? `ADDR_WIDTH'b0 : iWrAddr;
    assign w_wdata             = (iReset) ? `DATA_WIDTH'b0 : iWrData;
    assign w_wstrb             = (iReset) ? `MASK_WIDTH'b0 : iWrMask;

    assign w_rd_start          = iRdValid;
    assign w_rd_last           = r_rready;
    assign w_rd_addr_handshake = pAXI4_ar_valid && pAXI4_ar_ready;
    assign w_rd_data_handshake = pAXI4_r_valid  && pAXI4_r_ready;
    assign w_wr_start          = iWrValid;
    assign w_wr_last           = w_wr_data_handshake;
    assign w_wr_addr_handshake = pAXI4_aw_valid && pAXI4_aw_ready;
    assign w_wr_data_handshake = pAXI4_w_valid  && pAXI4_w_ready;
    assign w_wr_resp_handshake = pAXI4_b_valid  && pAXI4_b_ready;

    //-------------------------------------------------------------------------
    always @(posedge iClock) begin
        if (iReset) begin
            r_arvalid <= 1'b0;
        end
        else if (w_rd_start) begin
            r_arvalid <= 1'b1;
        end
        else if (w_rd_addr_handshake) begin
            r_arvalid <= 1'b0;
        end
        else begin
            r_arvalid <= r_arvalid;
        end
    end

    always @(posedge iClock) begin
        if (iReset) begin
            r_araddr <= `ADDR_WIDTH'b0;
        end
        else if (w_rd_start) begin
            r_araddr <= iRdAddr;
        end
        else begin
            r_araddr <= r_araddr;
        end
    end

    always @(posedge iClock) begin
        if (iReset) begin
            r_rready <= 1'b0;
        end
        else if (w_rd_addr_handshake) begin
            r_rready <= 1'b1;
        end
        else if (w_rd_last) begin
            r_rready <= 1'b0;
        end
        else begin
            r_rready <= r_rready;
        end
    end

    always @(posedge iClock) begin
        if (iReset) begin
            r_rdata <= `DATA_WIDTH'b0;
        end
        else if (w_rd_data_handshake) begin
            r_rdata <= pAXI4_r_bits_data;
        end
        else begin
            r_rdata <= r_rdata;
        end
    end

    always @(posedge iClock) begin
        if (iReset) begin
            r_awvalid <= 1'b0;
        end
        else if (w_wr_start) begin
            r_awvalid <= 1'b1;
        end
        else if (w_wr_addr_handshake) begin
            r_awvalid <= 1'b0;
        end
        else begin
            r_awvalid <= r_awvalid;
        end
    end

    always @(posedge iClock) begin
        if (iReset) begin
            r_awaddr <= `ADDR_WIDTH'b0;
        end
        else if (w_wr_start) begin
            r_awaddr <= iWrAddr;
        end
        else begin
            r_awaddr <= r_awaddr;
        end
    end

    always @(posedge iClock) begin
        if (iReset) begin
            r_wvalid <= 1'b0;
        end
        else if (w_wr_addr_handshake) begin
            r_wvalid <= 1'b1;
        end
        else if (w_wr_last) begin
            r_wvalid <= 1'b0;
        end
        else begin
            r_wvalid <= r_wvalid;
        end
    end

    //-------------------------------------------------------------------------
    // always @(posedge iClock) begin
    //     if (iReset) begin
    //         r_state_rd_curr <= P_STATE_IDLE;
    //     end
    //     else begin
    //         r_state_rd_curr <= r_state_rd_next;
    //     end
    // end

    // always @(*) begin
    //     case (r_state_rd_curr)
    //         P_STATE_IDLE: begin
    //             if (w_rd_start) begin
    //                 r_state_rd_next = P_STATE_RD_ADDR;
    //             end
    //             else begin
    //                 r_state_rd_next = P_STATE_IDLE;
    //             end
    //         end
    //         P_STATE_RD_ADDR: begin
    //             r_state_rd_next = P_STATE_RD_DATA;
    //         end
    //         P_STATE_RD_DATA: begin
    //             if (w_rd_last) begin
    //                 r_state_rd_next = P_STATE_IDLE;
    //             end
    //             else begin
    //                 r_state_rd_next = P_STATE_RD_DATA;
    //             end
    //         end
    //         default: begin
    //             r_state_rd_next = P_STATE_IDLE;
    //         end
    //     endcase
    // end

    // always @(posedge iClock) begin
    //     if (iReset) begin
    //         r_state_wr_curr <= P_STATE_IDLE;
    //     end
    //     else begin
    //         r_state_wr_curr <= r_state_wr_next;
    //     end
    // end

    // always @(*) begin
    //     case (r_state_wr_curr)
    //         P_STATE_IDLE: begin
    //             if (w_wr_start) begin
    //                 r_state_wr_next = P_STATE_WR_ADDR;
    //             end
    //             else begin
    //                 r_state_wr_next = P_STATE_IDLE;
    //             end
    //         end
    //         P_STATE_WR_ADDR: begin
    //             r_state_wr_next = P_STATE_WR_DATA;
    //         end
    //         P_STATE_WR_DATA: begin
    //             if (w_wr_last) begin
    //                 r_state_wr_next = P_STATE_IDLE;
    //             end
    //             else begin
    //                 r_state_wr_next = P_STATE_WR_DATA;
    //             end
    //         end
    //         default: begin
    //             r_state_wr_next = P_STATE_IDLE;
    //         end
    //     endcase
    // end
endmodule
