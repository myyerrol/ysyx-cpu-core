`include "Config.v"

module AXI4LiteM(
    input  wire                       iClock,
    input  wire                       iReset,
    input  wire [`MODE_WIDTH - 1 : 0] iMode,
    input  wire [`ADDR_WIDTH - 1 : 0] iAddr,
    input  wire [`DATA_WIDTH - 1 : 0] iData,
    input  wire [`MASK_WIDTH - 1 : 0] iMask,
    output wire [`DATA_WIDTH - 1 : 0] oData,
    output wire [`RESP_WIDTH - 1 : 0] oResp,

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
        $display("[vtrace] state curr: %d, state next: %d", r_state_rd_curr, r_state_rd_next);
        $display("[vtrace] rd start:   %d, wr start:   %d", r_rd_start, r_wr_start);
        $display("[vtrace] clock:      %d, reset:      %d", iClock, iReset);
        $display("[vtrace] arvalid:    %d, arready:    %d, araddr: %x", pAXI4_ar_valid, pAXI4_ar_ready, pAXI4_ar_bits_addr);
        $display("[vtrace] rvalid:     %d, rready:     %d, rdata:  %x, rresp: %d", pAXI4_r_valid, pAXI4_r_ready, pAXI4_r_bits_data, pAXI4_r_bits_resp);
`endif
    end

    //-------------------------------------------------------------------------
    parameter P_STATE_IDLE     = 'd0;
    parameter P_STATE_RD_START = 'd1;
    parameter P_STATE_RD_TRANS = 'd2;
    parameter P_STATE_RD_END   = 'd3;
    parameter P_STATE_WR_START = 'd4;
    parameter P_STATE_WR_TRANS = 'd5;
    parameter P_STATE_WR_END   = 'd6;

    reg [2 : 0] r_state_rd_curr;
    reg [2 : 0] r_state_rd_next;

    reg [2 : 0] r_state_wr_curr;
    reg [2 : 0] r_state_wr_next;

    //-------------------------------------------------------------------------
    wire w_rd_last;
    wire w_wr_last;

    //-------------------------------------------------------------------------
    reg                       r_arvalid;
    reg [`ADDR_WIDTH - 1 : 0] r_araddr;
    reg                       r_rready;
    reg                       r_awvalid;
    reg [`ADDR_WIDTH - 1 : 0] r_awaddr;
    reg                       r_wvalid;
    reg [`DATA_WIDTH - 1 : 0] r_wdata;
    reg [`MASK_WIDTH - 1 : 0] r_wstrb;
    reg                       r_bready;

    reg                       r_rd_start;
    reg [`DATA_WIDTH - 1 : 0] r_rd_data;
    reg                       r_wr_start;

    //-------------------------------------------------------------------------
    assign oData              = pAXI4_r_bits_data;
    assign oResp              = (iMode === `MODE_RD) ? pAXI4_r_bits_resp :
                                                       pAXI4_b_bits_resp;

    assign pAXI4_ar_valid     = r_arvalid;
    assign pAXI4_ar_bits_addr = r_araddr;
    assign pAXI4_r_ready      = r_rready;
    assign pAXI4_aw_valid     = r_awvalid;
    assign pAXI4_aw_bits_addr = r_awaddr;
    assign pAXI4_w_valid      = r_wvalid;
    assign pAXI4_w_bits_data  = r_wdata;
    assign pAXI4_w_bits_strb  = r_wstrb;
    assign pAXI4_b_ready      = 1'b1;

    assign w_rd_last          = r_rready;
    assign w_wr_last          = pAXI4_w_valid && pAXI4_w_ready;

    //-------------------------------------------------------------------------
    always @(posedge iClock) begin
        if (iReset) begin
            r_arvalid <= 1'b0;
        end
        else if (r_rd_start) begin
            r_arvalid <= 1'b1;
        end
        else if (pAXI4_ar_valid && pAXI4_ar_ready) begin
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
        else if (r_rd_start) begin
            r_araddr <= iAddr;
        end
        else if (w_rd_last) begin
            r_araddr <= `ADDR_WIDTH'b0;
        end
        else begin
            r_araddr <= r_araddr;
        end
    end

    always @(posedge iClock) begin
        if (iReset) begin
            r_rready <= 1'b0;
        end
        else if (pAXI4_ar_valid && pAXI4_ar_ready) begin
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
            r_rd_data <= `DATA_WIDTH'b0;
        end
        else if (pAXI4_r_valid && pAXI4_r_ready) begin
            r_rd_data <= pAXI4_r_bits_data;
        end
        else if (w_rd_last) begin
            r_rd_data <= `DATA_WIDTH'b0;
        end
        else begin
            r_rd_data <= r_rd_data;
        end
    end

    always @(posedge iClock) begin
        if (iReset) begin
            r_awvalid <= 1'b0;
        end
        else if (r_wr_start) begin
            r_awvalid <= 1'b1;
        end
        else if (pAXI4_aw_valid && pAXI4_aw_ready) begin
            r_awvalid <= 1'b0;
        end
        else begin
            r_awvalid <= r_awvalid;
        end
    end

    always @(posedge iClock) begin
        if (iReset) begin
            r_awaddr <= `DATA_WIDTH'b0;
        end
        else if (r_wr_start) begin
            r_awaddr <= iAddr;
        end
        else if (w_wr_last) begin
            r_awaddr <= `DATA_WIDTH'b0;
        end
        else begin
            r_awaddr <= r_awaddr;
        end
    end

    always @(posedge iClock) begin
        if (iReset) begin
            r_wvalid <= 1'b0;
        end
        else if (pAXI4_aw_valid && pAXI4_aw_ready) begin
            r_wvalid <= 1'b1;
        end
        else if (w_wr_last) begin
            r_wvalid <= 1'b0;
        end
        else begin
            r_wvalid <= r_wvalid;
        end
    end

    always @(posedge iClock) begin
        if (iReset) begin
            r_wdata <= `DATA_WIDTH'b0;
        end
        else if (pAXI4_w_valid && pAXI4_w_ready) begin
            r_wdata <= iData;
        end
        else if (w_wr_last) begin
            r_wdata <= `DATA_WIDTH'b0;
        end
        else begin
            r_wdata <= r_wdata;
        end
    end

    always @(posedge iClock) begin
        if (iReset) begin
            r_wstrb <= `MASK_WIDTH'b0;
        end
        else if (pAXI4_w_valid && pAXI4_w_ready) begin
            r_wstrb <= iMask;
        end
        else if (w_wr_last) begin
            r_wstrb <= `MASK_WIDTH'b0;
        end
        else begin
            r_wstrb <= r_wstrb;
        end
    end

    // always @(posedge iClock) begin
    //     if (iReset) begin
    //         r_bready <= 1'b0;
    //     end
    //     else if (pAXI4_w_valid && pAXI4_w_ready) begin
    //         r_bready <= 1'b1;
    //     end
    //     else if (pAXI4_b_valid && pAXI4_b_ready) begin
    //         r_bready <= 1'b0;
    //     end
    //     else begin
    //         r_bready <= r_bready;
    //     end
    // end

    //-------------------------------------------------------------------------
    always @(posedge iClock) begin
        if (iReset) begin
            r_state_rd_curr <= P_STATE_IDLE;
        end
        else begin
            r_state_rd_curr <= r_state_rd_next;
        end
    end

    always @(*) begin
        case (r_state_rd_curr)
            P_STATE_IDLE: begin
                if (iMode === `MODE_RD) begin
                    r_state_rd_next = P_STATE_RD_START;
                end
                else if (iMode === `MODE_RW) begin
                    if (r_state_wr_curr === P_STATE_WR_END) begin
                        r_state_rd_next = P_STATE_RD_START;
                    end
                    else begin
                        r_state_rd_next = P_STATE_IDLE;
                    end
                end
                else begin
                    r_state_rd_next = P_STATE_IDLE;
                end
            end
            P_STATE_RD_START: begin
                if (r_rd_start) begin
                    r_state_rd_next = P_STATE_RD_TRANS;
                end
                else begin
                    r_state_rd_next = P_STATE_RD_START;
                end
            end
            P_STATE_RD_TRANS: begin
                if (w_rd_last) begin
                    r_state_rd_next = P_STATE_RD_END;
                end
                else begin
                    r_state_rd_next = P_STATE_RD_TRANS;
                end
            end
            P_STATE_RD_END: begin
                r_state_rd_next = P_STATE_IDLE;
            end
            default: begin
                r_state_rd_next = P_STATE_IDLE;
            end
        endcase
    end

    always @(*) begin
        if (r_state_rd_curr === P_STATE_RD_START) begin
            r_rd_start = 1'b1;
        end
        else begin
            r_rd_start = 1'b0;
        end
    end

    always @(posedge iClock) begin
        if (iReset) begin
            r_state_wr_curr <= P_STATE_IDLE;
        end
        else begin
            r_state_wr_curr <= r_state_wr_next;
        end
    end

    always @(*) begin
        case (r_state_wr_curr)
            P_STATE_IDLE: begin
                r_state_wr_next = P_STATE_WR_START;
            end
            P_STATE_WR_START: begin
                if (r_wr_start) begin
                    r_state_wr_next = P_STATE_WR_TRANS;
                end
                else begin
                    r_state_wr_next = P_STATE_WR_START;
                end
            end
            P_STATE_WR_TRANS: begin
                if (w_wr_last) begin
                    r_state_wr_next = P_STATE_WR_END;
                end
                else begin
                    r_state_wr_next = P_STATE_WR_TRANS;
                end
            end
            P_STATE_WR_END: begin
                if (iMode === `MODE_WR) begin
                    r_state_wr_next = P_STATE_IDLE;
                end
                else if (iMode === `MODE_RW) begin
                    if (r_state_rd_curr === P_STATE_RD_END) begin
                        r_state_wr_next = P_STATE_IDLE;
                    end
                    else begin
                        r_state_wr_next = P_STATE_WR_END;
                    end
                end
                else begin
                    r_state_wr_next = P_STATE_IDLE;
                end
            end
            default: begin
                r_state_wr_next = P_STATE_IDLE;
            end
        endcase
    end

    always @(posedge iClock) begin
        if (r_state_wr_curr === P_STATE_WR_START) begin
            r_wr_start <= 1'b1;
        end
        else begin
            r_wr_start <= 1'b0;
        end
    end

endmodule
