module counter_seg (
    input  wire [3:0] i_num,
    output reg  [6:0] o_seg
);

    always @(*) begin
        case (i_num)
            4'b0000: o_seg = 7'b0000001;
            4'b0001: o_seg = 7'b1001111;
            4'b0010: o_seg = 7'b0010010;
            4'b0011: o_seg = 7'b0000110;
            4'b0100: o_seg = 7'b1001100;
            4'b0101: o_seg = 7'b0100100;
            4'b0110: o_seg = 7'b0100000;
            4'b0111: o_seg = 7'b0001111;
            4'b1000: o_seg = 7'b0000000;
            4'b1001: o_seg = 7'b0000100;
            4'b1010: o_seg = 7'b0001000;
            4'b1011: o_seg = 7'b1100000;
            4'b1100: o_seg = 7'b0110001;
            4'b1101: o_seg = 7'b1000010;
            4'b1110: o_seg = 7'b0110000;
            4'b1111: o_seg = 7'b0111000;
            default: o_seg = 7'b0000000;
        endcase
    end

endmodule
