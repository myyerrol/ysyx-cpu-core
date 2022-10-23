module char_ctr(
    input wire [9:0] i_h_addr, // 像素横坐标
    input wire [9:0] i_v_addr  // 像素纵坐标
);

    reg [7:0] t_char_h_addr = 8'b0;
    reg [7:0] t_char_v_addr = 8'b0;

    reg [11:0] t_vga_mem_addr = 12'b0;
    reg [7:0]  t_vga_mem[30*70-1:0];

    always @(*) begin
        $display("v_addr: %x, h_addr: %x", i_v_addr, i_h_addr);

        // 计算显存的X地址

        // 计算显存的Y地址
    end

endmodule
