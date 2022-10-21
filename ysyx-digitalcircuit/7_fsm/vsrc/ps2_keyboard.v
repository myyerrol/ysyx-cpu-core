module ps2_keyboard(
    input wire [0:0] i_clk,
    input wire [0:0] i_clr_n,
    input wire [0:0] i_ps2_clk,
    input wire [0:0] i_ps2_data,
    input wire [0:0] i_nextdata_n,
    output reg [7:0] o_ps2_data,
    output reg [0:0] o_ps2_ready,
    output reg [0:0] o_ps2_overflow,
    output reg [7:0] o_ps2_count
);

    reg [9:0] t_buffer;
    reg [7:0] t_fifo[7:0];
    reg [2:0] t_fifo_wt_ptr;
    reg [2:0] t_fifo_rd_ptr;
    reg [3:0] t_ps2_data_count;
    reg [2:0] t_ps2_clk_sync;

    // 通过向左移位的方式每次将最新的i_ps2_clk保存在最低位，此时t_ps2_clk_sync从低到高
    // 依次保存有i_ps2_clk最新的3个数值
    always @(posedge i_clk) begin
        t_ps2_clk_sync <=  { t_ps2_clk_sync[1:0], i_ps2_clk };
    end

    // 判断t_ps2_clk_sync是否处于下降沿
    wire [0:0] t_sampling = t_ps2_clk_sync[2] & ~t_ps2_clk_sync[1];

    always @(posedge i_clk) begin
        if (i_clr_n == 1'b0) begin
            o_ps2_overflow <= 1'b0;
            o_ps2_ready <= 1'b0;
            o_ps2_count <= 8'b0;
            t_fifo_wt_ptr <= 3'b0;
            t_fifo_rd_ptr <= 3'b0;
            t_ps2_data_count <= 4'b0;
        end
        else begin
            // 读取数据
            if (o_ps2_ready == 1'b1) begin
                if (i_nextdata_n == 1'b0) begin
                    t_fifo_rd_ptr <= t_fifo_rd_ptr + 3'b1;
                    if (t_fifo_wt_ptr == (t_fifo_rd_ptr + 3'b1)) begin
                        o_ps2_ready <= 1'b0;
                    end
                end
            end
            // 采集数据
            if (t_sampling == 1'b1) begin
                // 从缓冲区获取数据并进行处理
                if (t_ps2_data_count == 4'd10) begin
                    if ((t_buffer[0] == 1'b0) &&                // 起始位
                        (i_ps2_data == 1'b1)  &&                // 停止位
                        (^t_buffer[9:1])) begin                 // 校验位
                        $display("scan code: %x", t_buffer[8:1]);
                        t_fifo[t_fifo_wt_ptr] <= t_buffer[8:1];
                        if (t_buffer[8:1] == 8'hf0) begin
                            o_ps2_count <= o_ps2_count + 1'b1;
                            // t_fifo[t_fifo_wt_ptr] <= 8'b0;
                        end
                        else begin
                        end
                        t_fifo_wt_ptr <= t_fifo_wt_ptr + 3'b1;
                        o_ps2_ready <= 1'b1;
                        o_ps2_overflow <=
                            o_ps2_overflow |
                            (t_fifo_rd_ptr == (t_fifo_wt_ptr + 3'b1));
                    end
                    t_ps2_data_count <= 4'b0;
                end
                // 将数据存储到缓冲区
                else begin
                    t_buffer[t_ps2_data_count] <= i_ps2_data;
                    t_ps2_data_count <= t_ps2_data_count + 3'b1;
                end
            end
        end
    end

    assign o_ps2_data = t_fifo[t_fifo_rd_ptr];

endmodule
