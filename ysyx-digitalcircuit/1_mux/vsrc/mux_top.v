module mux_top(
    input  [1:0] x0,
    input  [1:0] x1,
    input  [1:0] x2,
    input  [1:0] x3,
    input  [1:0] y,
    output [1:0] f
);

    mux_key_default #(4, 2, 2) mux_key_default_inst0(y, 2'b00, {
        2'b00, x0,
        2'b01, x1,
        2'b10, x2,
        2'b11, x3
    }, f);

endmodule
