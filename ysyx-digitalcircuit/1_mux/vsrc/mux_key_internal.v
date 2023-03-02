module mux_key_internal #(NR_KEY = 2, KEY_LEN = 1, DATA_LEN = 1, HAS_DEFAULT = 0) (
    input  wire [KEY_LEN - 1 : 0]                       key,
    input  wire [DATA_LEN - 1 : 0]                      default_out,
    input  wire [NR_KEY * (KEY_LEN + DATA_LEN) - 1 : 0] lut,
    output reg  [DATA_LEN - 1 : 0]                      out
);

    localparam PAIR_LEN = KEY_LEN + DATA_LEN;
    wire [PAIR_LEN - 1 : 0] pair_list[NR_KEY - 1 : 0];
    wire [KEY_LEN - 1 : 0]  key_list[NR_KEY - 1 : 0];
    wire [DATA_LEN - 1 : 0] data_list[NR_KEY - 1 : 0];

    generate
        for (genvar n = 0; n < NR_KEY; n = n + 1) begin
            assign pair_list[n] = lut[PAIR_LEN * (n + 1) - 1 : PAIR_LEN * n];
            assign key_list[n]  = pair_list[n][PAIR_LEN - 1 : DATA_LEN];
            assign data_list[n] = pair_list[n][DATA_LEN - 1 : 0];
        end
    endgenerate

    reg [DATA_LEN - 1 : 0] lut_out;
    reg hit;
    integer i;

    always @(*) begin
        lut_out = 0;
        hit = 0;
        for (i = 0; i < NR_KEY; i = i + 1) begin
            lut_out = lut_out | ({DATA_LEN{key == key_list[i]}} & data_list[i]);
            hit = hit | (key == key_list[i]);
        end
        if (!HAS_DEFAULT) begin
            out = lut_out;
        end
        else begin
            out = (hit ? lut_out : default_out);
        end
    end

endmodule
