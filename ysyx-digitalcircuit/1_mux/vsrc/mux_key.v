module mux_key #(NR_KEY = 2, KEY_LEN = 1, DATA_LEN = 1) (
    input  [KEY_LEN - 1 : 0]                       key,
    input  [NR_KEY * (KEY_LEN + DATA_LEN) - 1 : 0] lut,
    output [DATA_LEN - 1 : 0]                      out
);
    mux_key_internal #(NR_KEY, KEY_LEN, DATA_LEN, 0) i0(key, {DATA_LEN{1'b0}}, lut, out);
endmodule
