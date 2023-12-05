// `define BTRACE_MEMORY
// `define BTRACE_MONITOR

`define RESP_WIDTH 2
`define BYTE_WIDTH 8
`define INST_WIDTH 32
`define DATA_WIDTH 64

`define MEMS_NUM 64'h00008000
`define ADDR_SIM 64'h80000000

`define MEM_BYT_1_U 10'd1
`define MEM_BYT_2_U 10'd2
`define MEM_BYT_4_U 10'd3
`define MEM_BYT_8_U 10'd4

`define AXI4_RRESP_OKEY   2'b00
`define AXI4_RRESP_EXOKAY 2'b01
`define AXI4_RRESP_SLVEER 2'b10
`define AXI4_RRESP_DECEER 2'b11
