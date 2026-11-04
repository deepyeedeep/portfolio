module CSA #(parameter BW = 8)(
input [BW-1:0] x1, x2, 
input cin,
output [BW-1:0] sum,
output cout, ccout
);
parameter numFA4 = BW/4;

wire [BW-1:0] sum0, sum1;
wire car0[0:numFA4], car1[0:numFA4], ccar0[0:numFA4-1], ccar1[0:numFA4-1];

wire prc[0:numFA4];

assign prc[0] = cin;
assign cout = prc[numFA4];
assign ccout = prc[numFA4-1] ? ccar1[numFA4-1] : ccar0[numFA4-1];
generate
genvar i;
for(i = 0; i < numFA4; i = i+1)begin: CSAmodule
CLA #4 ad1(x1[4*i+3:4*i], x2[4*i+3:4*i], 1'b1, sum1[4*i+3:4*i], car1[i], ccar1[i]);
CLA #4 ad0(x1[4*i+3:4*i], x2[4*i+3:4*i], 1'b0, sum0[4*i+3:4*i], car0[i], ccar0[i]);

assign sum[4*i+3:4*i] = prc[i] ? sum1[4*i+3:4*i] : sum0[4*i+3:4*i];
assign prc[i+1] = prc[i] ? car1[i] : car0[i];
end
endgenerate
endmodule