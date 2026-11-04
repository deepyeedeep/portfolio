module FAwide #(parameter BW = 8)(
input [BW:0] A, B, Cin,
output [BW:0] S, Cout
);

generate
genvar i ;
for(i = 0; i <= BW; i = i+1) begin: FA
FA1 (A[i], B[i], Cin[i], S[i], Cout[i]);
end
endgenerate

endmodule