module cpu_dp(clk, rstn, Din, Cin, CW, PrgAddr, Aout, Dout, PSW);//PSW=V,C,N,Z;  

parameter BW = 16;

input clk, rstn;
input [16:0] CW;
input [7:0] PrgAddr;//current program address
input [BW-1:0] Din, Cin;
output [BW-1:0] Aout, Dout;
output [3:0] PSW;

wire [BW-1:0] busA, busB, busD, Fout, Bout;   //busD=D data
wire [2:0] DA, AA, BA;
wire [4:0] FS;
wire RW, MB, MD, MM;

assign DA = CW [16:14];
assign AA = CW [13:11];
assign BA = CW [10:8];
assign MB = CW [7];
assign FS = CW [6:2];
assign MD = CW [1];
assign RW = CW [0];

assign Dout = busB;

Funit #(BW) u0 (busA, busB, FS, PSW, Fout);
Rfile #(BW) u1 (clk, rstn, DA, AA, BA, RW, busD, busA, Bout);

assign busB = MB ? Cin : Bout;//MUX B, @Dara Ram, Data in
assign busD = MD ? Din : Fout;//MUX D, Register File Data input
assign Aout = MM ? {{(BW-7){1'b0}}, PrgAddr} : busA;

endmodule
