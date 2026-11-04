module ALU (A, B, GS, Cout, CCout, ALU_Result);

parameter BW = 16;	 
input [BW-1:0] A, B;
input [3:0] GS;
output [BW-1:0] ALU_Result;
output Cout, CCout;

wire [BW-1:0] ALU_Result;
wire [BW-1:0] Aout, Lout;

AC u1 (A,B,GS[2:1],GS[0],Aout, Cout, CCout);
LC u2 (A,B,GS[2:1],Lout);

assign ALU_Result = GS[3] ? Lout : Aout ;

endmodule

