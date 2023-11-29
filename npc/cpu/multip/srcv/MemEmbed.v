`include "Config.v"

module MemEmbed(
    input  wire                      iClock,
    input  wire                      iReset,
    input  wire                      bMEMPortDualIO_iRdEn,
    input  wire                      bMEMPortDualIO_iWrEn,
    input  wire[`DATA_WIDTH - 1 : 0] bMEMPortDualIO_iAddr,
    input  wire[`DATA_WIDTH - 1 : 0] bMEMPortDualIO_iWrData,
    input  wire[`DATA_WIDTH - 1 : 0] bMEMPortDualIO_iWrByt,

    output  reg[`DATA_WIDTH - 1 : 0] bMEMPortDualIO_oRdData
);

    reg[`DATA_WIDTH - 1 : 0] mem[`MEMS_NUM];

    initial begin
        integer i;
        $readmemh("/home/myyerrol/Workspaces/mem.txt", mem);
`ifdef BTRACE_MEMORY
        for (i = 0; i < `MEMS_NUM; i++) begin
            $display("[btrace] mem[%d]: %x", i, mem[i]);
        end
`endif
`ifdef BTRACE_MONITOR
        $monitor("[btrace] iaddr: %x, addr: %x, mem[addr]: %x",
                 bMEMPortDualIO_iAddr,
                 addr,
                 mem[addr]);
`endif
    end

    wire [`DATA_WIDTH - 1 : 0] addr;
    wire [`DATA_WIDTH - 1 : 0] wr_data;
    assign addr = (bMEMPortDualIO_iAddr - `ADDR_SIM) / 4;
    assign wr_data = bMEMPortDualIO_iWrData;

    always @(bMEMPortDualIO_iAddr or bMEMPortDualIO_iWrData) begin
        if (bMEMPortDualIO_iRdEn) begin
            bMEMPortDualIO_oRdData <= mem[addr];
        end
        if (bMEMPortDualIO_iWrEn) begin
            case (bMEMPortDualIO_iWrByt)
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
