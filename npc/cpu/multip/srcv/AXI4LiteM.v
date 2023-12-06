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
    parameter STATE_IDLE     = 'd0;
    parameter STATE_RD_START = 'd1;
    parameter STATE_RD_TRANS = 'd2;
    parameter STATE_RD_END   = 'd3;
    parameter STATE_WR_START = 'd4;
    parameter STATE_WR_TRANS = 'd5;
    parameter STATE_WR_END   = 'd6;

    reg [3 : 0] r_state_rd_curr;
    reg [3 : 0] r_state_rd_next;

    reg [3 : 0] r_state_wr_curr;
    reg [3 : 0] r_state_wr_next;

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

    reg                       r_rd_start;
    reg                       r_rd_last;
    reg                       r_wr_start;
    reg                       r_wr_last;
    //-------------------------------------------------------------------------
    assign pAXI4M_ar_valid     = r_arvalid;
    assign pAXI4M_ar_bits_addr = r_araddr;

    assign pAXI4M_r_ready      = r_rready;

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
        else if (r_rd_start) begin
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
        else if (r_rd_start) begin
            r_araddr <= iAddr;
        end
        else begin
            r_araddr <= r_araddr;
        end
    end

    always @(posedge iClock) begin
        if (!iReset) begin
            r_rready <= 1'b0;
        end
        else if (pAXI4M_ar_valid && pAXI4M_ar_ready) begin
            r_rready <= 1'b1;
        end
        else if (r_rready) begin
            r_rready <= 1'b0;
        end
        else begin
            r_rready <= r_rready;
        end
    end

    always @(posedge iClock) begin
        if (pAXI4M_r_valid && pAXI4M_r_ready) begin
            r_rdata <= pAXI4M_r_bits_data;
        end
        else begin
            r_rdata <= r_rdata;
        end
    end

    always @(posedge iClock) begin
        if (!iReset) begin
            r_awvalid <= 1'b0;
        end
        else if (r_wr_start) begin
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
        else if (r_wr_start) begin
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
    //     else if (pAXI4M_aw_valid && pAXI4M_aw_ready) begin
    //         r_bready <= 1'b1;
    //     end
    //     else begin
    //         r_bready <= r_bready;
    //     end
    // end

    //-------------------------------------------------------------------------
    always @(posedge iClock) begin
        if (!iReset) begin
            r_state_rd_curr <= STATE_IDLE;
        end
        else begin
            r_state_rd_curr <= r_state_rd_next;
        end
    end

    always @(*) begin
        case (r_state_rd_curr)
            STATE_IDLE: begin
                if (r_state_wr_curr === STATE_WR_END) begin
                    r_state_rd_next = STATE_RD_START;
                end
                else begin
                    r_state_rd_next = STATE_IDLE;
                end
            end
            STATE_RD_START: begin
                if (r_rd_start) begin
                    r_state_rd_next = STATE_RD_TRANS;
                end
                else begin
                    r_state_rd_next = STATE_RD_START;
                end
            end
            STATE_RD_TRANS: begin
                if (r_rd_last) begin
                    r_state_rd_next = STATE_RD_END;
                end
                else begin
                    r_state_rd_next = STATE_RD_TRANS;
                end
            end
            STATE_RD_END: begin
                r_state_rd_next = STATE_IDLE;
            end
            default: begin
                r_state_rd_next = STATE_IDLE;
            end
        endcase
    end

    always @(posedge iClock) begin
        if (r_state_rd_curr === STATE_RD_START) begin
            r_rd_start <= 1'b1;
        end
        else begin
            r_rd_start <= 1'b0;
        end
    end

    always @(posedge iClock) begin
        if (!iReset) begin
            r_state_wr_curr <= STATE_IDLE;
        end
        else begin
            r_state_wr_curr <= r_state_wr_next;
        end
    end

    always @(*) begin
        case (r_state_wr_curr)
            STATE_IDLE: begin
                if (r_state_rd_curr === STATE_RD_END) begin
                    r_state_wr_next = STATE_WR_START;
                end
                else begin
                    r_state_wr_next = STATE_IDLE;
                end
            end
            STATE_WR_START: begin
                if (r_wr_start) begin
                    r_state_wr_next = STATE_WR_TRANS;
                end
                else begin
                    r_state_wr_next = STATE_WR_START;
                end
            end
            STATE_WR_TRANS: begin
                if (r_wr_last) begin
                    r_state_wr_next = STATE_WR_END;
                end
                else begin
                    r_state_wr_next = STATE_WR_TRANS;
                end
            end
            STATE_WR_END: begin
                r_state_wr_next = STATE_IDLE;
            end
            default: begin
                r_state_wr_next = STATE_IDLE;
            end
        endcase
    end

    always @(posedge iClock) begin
        if (r_state_wr_curr === STATE_WR_START) begin
            r_wr_start <= 1'b1;
        end
        else begin
            r_wr_start <= 1'b0;
        end
    end

endmodule
