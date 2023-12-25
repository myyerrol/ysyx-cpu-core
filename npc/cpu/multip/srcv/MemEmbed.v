`include "Config.v"

module MemEmbed(
    input  wire                       iClock,
    input  wire                       iReset,
    input  wire                       pMem_iRdEn,
    input  wire                       pMem_iWrEn,
    input  wire [`DATA_WIDTH - 1 : 0] pMem_iAddr,
    input  wire [`DATA_WIDTH - 1 : 0] pMem_iWrData,
    input  wire [`SIGS_WIDTH - 1 : 0] pMem_iWrByt,

    output reg  [`DATA_WIDTH - 1 : 0] pMem_oRdData
);

    wire [`DATA_WIDTH - 1 : 0] w_vir_addr;
    wire [`DATA_WIDTH - 1 : 0] w_phy_addr_t;
    wire [`MEMS_WIDTH - 1 : 0] w_phy_addr;
    wire [`DATA_WIDTH - 1 : 0] w_wr_data;

    assign w_vir_addr   = pMem_iAddr;
    assign w_phy_addr_t = (w_vir_addr - `ADDR_INIT) / 4;
    assign w_phy_addr   = w_phy_addr_t[`MEMS_WIDTH - 1 : 0];
    assign w_wr_data    = pMem_iWrData;

    reg [`DATA_WIDTH - 1 : 0] r_mem[`MEMS_NUM];

    initial begin
        integer i;
        $readmemh("/home/myyerrol/Workspaces/oscc-cpu/mem.txt", r_mem);
`ifdef VTRACE_MEMORY
        for (i = 0; i < `MEMS_NUM; i++) begin
            $display("[vtrace] mem[%d]: %x", i, r_mem[i]);
        end
`endif
    end

    always @(*) begin
`ifdef VTRACE_MONITOR
        $display("[vtrace] addr: %x, addr: %x, mem[addr]: %x",
                 pMem_iAddr,
                 w_phy_addr,
                 r_mem[w_phy_addr]);
`endif
    end

    always @(pMem_iAddr or pMem_iWrData) begin
        if (pMem_iRdEn) begin
            pMem_oRdData <= r_mem[w_phy_addr];
        end
        if (pMem_iWrEn) begin
            case (pMem_iWrByt)
                `MEM_BYT_1_U: begin
                    r_mem[w_phy_addr] <= {w_wr_data[63 : 08], w_wr_data[07 : 0]};
                end
                `MEM_BYT_2_U: begin
                    r_mem[w_phy_addr] <= {w_wr_data[63 : 16], w_wr_data[15 : 0]};
                end
                `MEM_BYT_4_U: begin
                    r_mem[w_phy_addr] <= {w_wr_data[63 : 32], w_wr_data[31 : 0]};
                end
                `MEM_BYT_8_U: begin
                    r_mem[w_phy_addr] <= w_wr_data;
                end
                default: begin
                end
            endcase
        end
    end
endmodule
