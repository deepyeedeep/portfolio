module mbMUX(
input x1,
input x0,
input yDec1, yDec2, yDecS,
output oMux
);

wire y1sel, y2sel;

assign y1sel = x0 & yDec1;
assign y2sel = x1 & yDec2;
assign oMux = ( y1sel | y2sel ) ^ yDecS;

endmodule