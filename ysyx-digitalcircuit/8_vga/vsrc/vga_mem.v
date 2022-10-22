module vga_mem(
    input  wire [9:0]  i_h_addr,
    input  wire [8:0]  i_v_addr,
    output wire [23:0] o_vga_data
);

    reg [23:0] vga_mem[524287:0];

    initial begin
        $readmemh("picture.hex", vga_mem);
    end

assign o_vga_data = vga_mem[{ i_h_addr, i_v_addr }];

endmodule
