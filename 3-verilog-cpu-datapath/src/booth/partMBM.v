module partMBM #(parameter BW = 8) (
input [BW-1:0] x, y,
output [2*BW-1:0] outA, outB
);
wire [BW+1:0] mbmX = {x[BW-1], x, 1'b0};
wire [BW:0] mbmY = {y, 1'b0};

wire [BW:0] outMUX[BW/2-1:0];

wire yDec1[BW/2:0];
wire yDec2[BW/2:0];
wire yDecs[BW/2:0];

wire [BW:0] sAdder[BW/2-2:0];
wire [BW:0] cAdder[BW/2-2:0];
generate
genvar i;
for(i = 0; i < BW/2; i = i+1) begin: partMult
mbMUXwide #(BW)(mbmX, yDec1[i], yDec2[i], yDecs[i], outMUX[i]);
mbDEC (mbmY[2*i+2:2*i], yDec1[i], yDec2[i], yDecs[i]);
end

for(i = 0; i < (BW/2 - 2); i = i+1) begin: FA
FAwide #(BW) (
outMUX[i+2], {sAdder[i][BW], sAdder[i][BW], sAdder[i][BW:2]}, {cAdder[i][BW], cAdder[i][BW:1]},
sAdder[i+1], cAdder[i+1]
);
if(i == (BW/2 - 3)) begin
assign outA[2*BW-1:BW-2] = {sAdder[i+1][BW], sAdder[i+1]};
assign outB[2*BW-1:BW-2] = {cAdder[i+1], yDecs[i+2]};
end
else begin
assign outA[2*i+5:2*i+4] = sAdder[i+1][1:0];
assign outB[2*i+5:2*i+4] = {cAdder[i+1][0], yDecs[i+2]};
end
end

endgenerate

FAwide #(BW) ad0 (
{outMUX[0][BW], outMUX[0][BW], outMUX[0][BW:2]}, outMUX[1], (BW+1)*{1'b0},
sAdder[0], cAdder[0]
);
assign outA[1:0] = outMUX[0][1:0];
assign outA[3:2] = sAdder[0][1:0];
assign outB[1:0] = {1'b0, yDecs[0]};
assign outB[3:2] = {cAdder[0][0], yDecs[1]};


endmodule