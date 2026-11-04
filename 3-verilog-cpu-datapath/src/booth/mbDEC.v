module mbDEC(
input [2:0] y,
output reg yDec1, yDec2, yDecs
);

always@(*) begin
case (y)
3'b000: begin
	yDec1 = 1'b0;
	yDec2 = 1'b0;
	yDecs = 1'b0;
end
3'b001: begin
	yDec1 = 1'b1;
	yDec2 = 1'b0;
	yDecs = 1'b0;
end
3'b010: begin
	yDec1 = 1'b1;
	yDec2 = 1'b0;
	yDecs = 1'b0;
end
3'b011: begin
	yDec1 = 1'b0;
	yDec2 = 1'b1;
	yDecs = 1'b0;
end
3'b100: begin
	yDec1 = 1'b0;
	yDec2 = 1'b1;
	yDecs = 1'b1;
end
3'b101: begin
	yDec1 = 1'b1;
	yDec2 = 1'b0;
	yDecs = 1'b1;
end
3'b110: begin
	yDec1 = 1'b1;
	yDec2 = 1'b0;
	yDecs = 1'b1;
end
3'b111: begin
	yDec1 = 1'b0;
	yDec2 = 1'b0;
	yDecs = 1'b0;
end
default: begin
	yDec1 = 1'b0;
	yDec2 = 1'b0;
	yDecs = 1'b0;
end
endcase
end

endmodule