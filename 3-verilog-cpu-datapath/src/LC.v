module LC (A,B,S,Lout);

parameter BW = 16;

input [BW-1:0] A,B;
input [1:0] S;
output reg [BW-1:0] Lout;

always @(*)
    begin
        case(S)
		  2'b00: Lout = A & B;
		  2'b01: Lout = A | B;
		  2'b10: Lout = A ^ B;
		  2'b11: Lout = ~A;
		  endcase
		  end
endmodule
