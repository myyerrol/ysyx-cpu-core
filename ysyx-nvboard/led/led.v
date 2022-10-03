module led(
    input  wire       clk,
    input  wire       rst,
    output reg [15:0] led_out
);

reg [31:0] count;

always @(posedge clk) begin
    if (rst) begin
        led_out <= 16'b1;
        count <= 32'b0;
    end
    else begin
        if (count == 32'b0) begin
            led_out <= { led_out[14:0], led_out[15] };
        end
        count <= (count >= 5000000 ? 32'b0 : count + 1);
    end
end

endmodule
