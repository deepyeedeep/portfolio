module mbMUXwide #(parameter BW = 8)(
input [BW+1:0] mbmX,
input yDec1, yDec2, yDecs,
output [BW:0] outMUX
);

generate
genvar i;
for(i = 0; i < BW+1; i = i+1) begin: wideMUX
mbMUX (mbmX[i], mbmX[i+1], yDec1, yDec2, yDecs, outMUX[i]);
end
endgenerate

endmodule