`include "Config.v"

module MemEmbed(
    input  wire                      iClock,
    input  wire                      iReset,
    input  wire                      pMem_iRdEn,
    input  wire                      pMem_iWrEn,
    input  wire[`DATA_WIDTH - 1 : 0] pMem_iAddr,
    input  wire[`DATA_WIDTH - 1 : 0] pMem_iWrData,
    input  wire[`DATA_WIDTH - 1 : 0] pMem_iWrByt,

    output reg [`DATA_WIDTH - 1 : 0] pMem_oRdData
);

    reg[`DATA_WIDTH - 1 : 0] mem[`MEMS_NUM];

    initial begin
        integer i;
        $readmemh("/home/myyerrol/Workspaces/oscc-cpu/mem.txt", mem);
`ifdef VTRACE_MEMORY
        for (i = 0; i < `MEMS_NUM; i++) begin
            $display("[vtrace] mem[%d]: %x", i, mem[i]);
        end
`endif
    end

    always @(*) begin
`ifdef VTRACE_MONITOR
        $display("[vtrace] iaddr: %x, addr: %x, mem[addr]: %x",
                 pMem_iAddr,
                 addr,
                 mem[addr]);
`endif
    end

    wire [`DATA_WIDTH - 1 : 0] addr;
    wire [`DATA_WIDTH - 1 : 0] wr_data;
    assign addr = (pMem_iAddr - `ADDR_SIM) / 4;
    assign wr_data = pMem_iWrData;

    always @(pMem_iAddr or pMem_iWrData) begin
        if (pMem_iRdEn) begin
            pMem_oRdData <= mem[addr];
        end
        if (pMem_iWrEn) begin
            case (pMem_iWrByt)
                `MEM_BYT_1_U: begin
                    mem[addr] <= {wr_data[63 : 08], wr_data[07 : 0]};
                end
                `MEM_BYT_2_U: begin
                    mem[addr] <= {wr_data[63 : 16], wr_data[15 : 0]};
                end
                `MEM_BYT_4_U: begin
                    mem[addr] <= {wr_data[63 : 32], wr_data[31 : 0]};
                end
                `MEM_BYT_8_U: begin
                    mem[addr] <= wr_data;
                end
                default: begin
                end
            endcase
        end
    end
endmodule
