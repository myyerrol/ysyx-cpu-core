module vga_ctrl (
    input  wire [0:0]  i_pclk,     // 时钟信号
    input  wire [0:0]  i_reset,    // 置位信号
    input  wire [23:0] i_vga_data, // 颜色数据
    output wire [9:0]  o_h_addr,   // 像素横坐标
    output wire [9:0]  o_v_addr,   // 像素纵坐标
    output wire [0:0]  o_hsync,    // 行同步信号
    output wire [0:0]  o_vsync,    // 列同步信号
    output wire [0:0]  o_valid,    // 消隐信号
    output wire [7:0]  o_vga_r,    // 红色信号
    output wire [7:0]  o_vga_g,    // 绿色信号
    output wire [7:0]  o_vga_b     // 蓝色信号
);

    // 640x480分辨率下的VGA参数设置
    parameter h_frontporch = 96;
    parameter h_active     = 144;
    parameter h_backporch  = 784;
    parameter h_total      = 800;

    parameter v_frontporch = 2;
    parameter v_active     = 35;
    parameter v_backporch  = 515;
    parameter v_total      = 525;

    reg  [9:0] x_cnt;
    reg  [9:0] y_cnt;
    wire [0:0] h_valid;
    wire [0:0] v_valid;

    always @(posedge i_pclk) begin
        if(i_reset == 1'b1) begin
            x_cnt <= 1;
            y_cnt <= 1;
        end
        else begin
            if(x_cnt == h_total) begin
                x_cnt <= 1;
                if (y_cnt == v_total) begin
                    y_cnt <= 1;
                end
                else begin
                    y_cnt <= y_cnt + 1;
                end
            end
            else begin
                x_cnt <= x_cnt + 1;
            end
        end
    end

    // 生成同步信号
    assign o_hsync = (x_cnt > h_frontporch);
    assign o_vsync = (y_cnt > v_frontporch);
    // 生成消隐信号
    assign h_valid = (x_cnt > h_active) & (x_cnt <= h_backporch);
    assign v_valid = (y_cnt > v_active) & (y_cnt <= v_backporch);
    assign o_valid = h_valid & v_valid;
    // 计算当前有效像素坐标
    assign o_h_addr = h_valid ? (x_cnt - 10'd145) : 10'd0;
    assign o_v_addr = v_valid ? (y_cnt - 10'd36) : 10'd0;
    // 设置输出的颜色值
    assign { o_vga_r, o_vga_g, o_vga_b } = i_vga_data;

endmodule
