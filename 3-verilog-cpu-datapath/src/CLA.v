module CLA #(parameter BW = 4)(
input [BW-1:0] x1, x2,
input cin,
output [BW-1:0] sum,
output cout, ccout);

wire [BW-1:0] P, G;
wire [BW:0] C;

assign C[0] = cin;
assign cout = C[BW];
assign ccout = C[BW-1];
generate
genvar i;
for(i=0; i < BW; i = i+1) begin: PG
HA1 HA(x1[i], x2[i], P[i], G[i]);
assign C[i+1] = G[i] | P[i] & C[i];
assign sum[i] = C[i] ^ P[i];
end

endgenerate
endmodule