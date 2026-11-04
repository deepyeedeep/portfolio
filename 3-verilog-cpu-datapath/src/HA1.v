module HA1(
input x1, x2,
output s, co
);

assign s = x1 ^ x2;
assign co = x1 & x2;

endmodule