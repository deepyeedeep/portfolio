module AC (A,B,S,Cin,Aout,Carry,CCarry);

parameter BW = 16;
input [BW-1:0] A,B;
input [1:0] S;
input Cin;
output [BW-1:0] Aout;
output Carry, CCarry;

wire [BW-1:0] y;
generate
genvar i;
for(i = 0; i < BW; i = i+1) begin: selector
assign y[i] = (B[i]&S[0])|(~B[i]&S[1]);
end
endgenerate
CSA #BW U(A, y, Cin, Aout, Carry, CCarry);

endmodule