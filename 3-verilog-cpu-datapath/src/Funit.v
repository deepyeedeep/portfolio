module Funit (busA, busB, FS, VCNZ, Fout);

parameter BW = 16;
input [BW-1:0] busA, busB;
input [4:0] FS;
output [BW-1:0] Fout;
output [3:0] VCNZ;

wire [BW-1:0] Gout, Hout;
wire IR, IL, MF, MSBC, PREC;

assign IR = 1'b0;
assign IL = 1'b0;
assign MF = FS[4];	

assign VCNZ[3] = (MSBC ^ PREC) ? 1'b1 : 1'b0;//overflow
assign VCNZ[2] = (MSBC) ? 1'b1 : 1'b0;//carry
assign VCNZ[1] = (Gout[BW-1]) ? 1'b1 : 1'b0;//negative sign
assign VCNZ[0] = (Gout == {BW{1'b0}}) ? 1'b1 : 1'b0;//zero fill


ALU r0 (busA, busB, FS[3:0], MSBC, PREC, Gout);
Shifter r1 (busB, IR, IL, FS[3:2], Hout);

assign Fout = MF ? Hout : Gout;

endmodule
