module mux_key_default #(NR_KEY = 2, KEY_LEN = 1, DATA_LEN = 1) (
    input  [KEY_LEN - 1 : 0]                       key,
    input  [DATA_LEN - 1 : 0]                      default_out,
    input  [NR_KEY * (KEY_LEN + DATA_LEN) - 1 : 0] lut,
    output [DATA_LEN - 1 : 0]                      out
);
    mux_key_internal #(NR_KEY, KEY_LEN, DATA_LEN, 1) mux_key_internal_inst0(
        key,
        default_out,
        lut,
        out);
endmodule
