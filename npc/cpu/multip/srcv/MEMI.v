module MEMPortDualI(
    input  wire       iClock,
    input  wire       iReset,
    input  wire       bMEMPortDualIO_iRdEn,
    input  wire       bMEMPortDualIO_iWrEn,
    input  wire[63:0] bMEMPortDualIO_iAddr,
    input  wire[63:0] bMEMPortDualIO_iWrData,
    input  wire[09:0] bMEMPortDualIO_iWrByt,

    output  reg[63:0] bMEMPortDualIO_oRdData
);

    reg[63:0] mem[4096];

    initial begin
        integer i;
        $readmemh("/home/myyerrol/Workspaces/mem.txt", mem);
        for (i = 0; i < 15; i++) begin
            $display("mem[%d]: %x", i, mem[i]);
        end
        $monitor("bMEMPortDualIO_iAddr: %x, addr: %x, mem[addr]: %x", bMEMPortDualIO_iAddr, addr, mem[addr]);
    end

    wire [63:0] addr;
    wire [63:0] wr_data;
    assign addr = bMEMPortDualIO_iAddr - 64'h80000000;
    assign wr_data = bMEMPortDualIO_iWrData;

    always @(bMEMPortDualIO_iAddr or bMEMPortDualIO_iWrData) begin
        if (bMEMPortDualIO_iRdEn) begin
            bMEMPortDualIO_oRdData <= mem[addr];
        end
        if (bMEMPortDualIO_iWrEn) begin
            case (bMEMPortDualIO_iWrByt)
                10'd1: begin
                    mem[addr] <= {wr_data[63:08], wr_data[07:0]};
                end
                10'd2: begin
                    mem[addr] <= {wr_data[63:16], wr_data[15:0]};
                end
                10'd3: begin
                    mem[addr] <= {wr_data[63:32], wr_data[31:0]};
                end
                10'd4: begin
                    mem[addr] <= wr_data;
                end
                default: begin
                end
            endcase
        end
    end
endmodule
