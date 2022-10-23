module ps2_keyboard_lut(
    input  wire [7:0] i_num,
    output reg  [7:0] o_num
);

    always @(*) begin
        case (i_num)
            8'h1c: o_num = 8'h61; // a
            8'h32: o_num = 8'h62; // b
            8'h21: o_num = 8'h63; // c
            8'h23: o_num = 8'h64; // d
            8'h24: o_num = 8'h65; // e
            8'h2b: o_num = 8'h66; // f
            8'h34: o_num = 8'h67; // g
            8'h33: o_num = 8'h68; // h
            8'h43: o_num = 8'h69; // i
            8'h3b: o_num = 8'h6a; // j
            8'h42: o_num = 8'h6b; // k
            8'h4b: o_num = 8'h6c; // l
            8'h3a: o_num = 8'h6d; // m
            8'h31: o_num = 8'h6e; // n
            8'h44: o_num = 8'h6f; // o
            8'h4d: o_num = 8'h70; // p
            8'h15: o_num = 8'h71; // q
            8'h2d: o_num = 8'h72; // r
            8'h1b: o_num = 8'h73; // s
            8'h2c: o_num = 8'h74; // t
            8'h3c: o_num = 8'h75; // u
            8'h2a: o_num = 8'h76; // v
            8'h1d: o_num = 8'h77; // w
            8'h22: o_num = 8'h78; // x
            8'h35: o_num = 8'h79; // y
            8'h1a: o_num = 8'h7a; // z
            8'h45: o_num = 8'h30; // 0
            8'h16: o_num = 8'h31; // 1
            8'h1e: o_num = 8'h32; // 2
            8'h26: o_num = 8'h33; // 3
            8'h25: o_num = 8'h34; // 4
            8'h2e: o_num = 8'h35; // 5
            8'h36: o_num = 8'h36; // 6
            8'h3d: o_num = 8'h37; // 7
            8'h3e: o_num = 8'h38; // 8
            8'h46: o_num = 8'h39; // 9
            default: o_num = 8'h00;
        endcase
    end

endmodule
