module Shifter (busB, IR, IL, HS, Out);

parameter BW = 16;
input [BW-1:0] busB;
input [1:0] HS;
input IR, IL;
output [BW-1:0] Out;

reg [BW-1:0] Out;

always @(*)
    begin
        case(HS)
		   2'b00: Out = busB ; 
			2'b01: Out = {IR,busB[BW-1]} ; 
			2'b10: Out = {busB[BW-3:0],IL} ; 
			default : Out = busB;
			endcase
	  end
endmodule
			