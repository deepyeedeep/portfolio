module Rfile (clk, rstn, DA, AA, BA, RW, Ddata, Adata, Bdata);

parameter BW = 16;
input clk, rstn;
input [3:0] DA, AA, BA;
input [BW-1:0] Ddata;
input RW;
output [BW-1:0] Adata, Bdata;

reg [BW-1:0] R0, R1, R2, R3, R4, R5, R6, R7;
reg [BW-1:0] R8;//temp usage register
reg [BW-1:0] Adata, Bdata;

always @ (posedge clk, negedge rstn)
if (!rstn) begin
   R0 <= {BW{1'b0}};
   R1 <= {BW{1'b0}};
   R2 <= {BW{1'b0}};
   R3 <= {BW{1'b0}};
   R4 <= {BW{1'b0}};
   R5 <= {BW{1'b0}};
   R6 <= {BW{1'b0}};
   R7 <= {BW{1'b0}};
	R8 <= {BW{1'b0}};
end
else if (RW)
  casex (DA)
	4'b1xxx: R8 <= Ddata;
   4'b0000: R0 <= Ddata;	
   4'b0001: R1 <= Ddata;	
   4'b0010: R2 <= Ddata;
   4'b0011: R3 <= Ddata;
   4'b0100: R4 <= Ddata;
   4'b0101: R5 <= Ddata;
	4'b0110: R6 <= Ddata;
	4'b0111: R7 <= Ddata;
	endcase
	
always @ *
  casex (AA)
   4'b1xxx: Adata = R8;	
   4'b0000: Adata = R0;
   4'b0001: Adata = R1;		
   4'b0010: Adata = R2;	
   4'b0011: Adata = R3;	
   4'b0100: Adata = R4;	
   4'b0101: Adata = R5;	
	4'b0110: Adata = R6;	
	4'b0111: Adata = R7;
	default: Adata = R0; 
	endcase
	
always @ *
  casex (BA)
   4'b1xxx: Bdata = R8;	
   4'b0000: Bdata = R0;
   4'b0001: Bdata = R1;		
   4'b0010: Bdata = R2;	
   4'b0011: Bdata = R3;	
   4'b0100: Bdata = R4;	
   4'b0101: Bdata = R5;	
	4'b0110: Bdata = R6;	
	4'b0111: Bdata = R7;
	default: Bdata = R0; 
	endcase
	
endmodule
