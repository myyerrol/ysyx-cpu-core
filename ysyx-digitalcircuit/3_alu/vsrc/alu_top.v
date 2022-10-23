module alu_top(
    input  wire [2:0] i_func,
    input  wire [3:0] i_num_a,
    input  wire [3:0] i_num_b,
    output reg  [3:0] o_num_c,
    output reg  [0:0] o_num_carry,
    output reg  [0:0] o_num_overflow,
    output reg  [0:0] o_num_zero
);

    always @(*) begin
        o_num_carry = 1'b0;
        o_num_overflow = 1'b0;
        o_num_zero = 1'b0;
        case (i_func)
            3'b000,
            3'b001: begin
                { o_num_carry, o_num_c } = i_num_a + i_num_b;
                assign o_num_overflow = (i_num_a[3] == i_num_b[3]) &&
                                        (o_num_c[3] != i_num_a[3]);
                assign o_num_zero = ~(|o_num_c);
            end
            3'b010:  o_num_c = ~i_num_a;
            3'b011:  o_num_c = i_num_a & i_num_b;
            3'b100:  o_num_c = i_num_a | i_num_b;
            3'b101:  o_num_c = i_num_a ^ i_num_b;
            3'b110:  o_num_c = (i_num_a < i_num_b) ? 4'b1 : 4'b0;
            3'b111:  o_num_c = (i_num_a == i_num_b) ? 4'b1 : 4'b0;
            default: o_num_c = 4'b0;
        endcase
    end

endmodule
