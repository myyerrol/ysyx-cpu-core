module ps2_keyboard_lut(
    input  wire [7:0] i_num,
    output reg  [7:0] o_num
);

    always @(*) begin
        case (i_num)
            8'h1c: o_num = 8'h61;
            8'h32: o_num = 8'h62;
            8'h21: o_num = 8'h63;
            8'h23: o_num = 8'h64;
            8'h24: o_num = 8'h65;
            8'h2b: o_num = 8'h66;
            8'h34: o_num = 8'h67;
            8'h33: o_num = 8'h68;
            8'h43: o_num = 8'h69;
            8'h3b: o_num = 8'h6a;
            8'h42: o_num = 8'h6b;
            8'h4b: o_num = 8'h6c;
            8'h3a: o_num = 8'h6d;
            8'h31: o_num = 8'h6e;
            8'h44: o_num = 8'h6f;
            8'h4d: o_num = 8'h70;
            8'h15: o_num = 8'h71;
            8'h2d: o_num = 8'h72;
            8'h1b: o_num = 8'h73;
            8'h2c: o_num = 8'h74;
            8'h3c: o_num = 8'h75;
            8'h2a: o_num = 8'h76;
            8'h1d: o_num = 8'h77;
            8'h22: o_num = 8'h78;
            8'h35: o_num = 8'h79;
            8'h1a: o_num = 8'h7a;
            8'h45: o_num = 8'h30;
            8'h16: o_num = 8'h31;
            8'h1e: o_num = 8'h32;
            8'h26: o_num = 8'h33;
            8'h25: o_num = 8'h34;
            8'h2e: o_num = 8'h35;
            8'h36: o_num = 8'h36;
            8'h3d: o_num = 8'h37;
            8'h3e: o_num = 8'h38;
            8'h46: o_num = 8'h39;
            default: o_num = 8'h00;
        endcase
    end

endmodule
