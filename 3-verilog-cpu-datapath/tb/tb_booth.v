`timescale 1ns/1ps
// Self-checking testbench for the radix-4 Booth multiplier (written in 2026)
// The original modules have unnamed instances (accepted by Quartus only).
// To simulate in ModelSim, add instance names first; see README.
module tb_booth;
  reg  signed [7:0]  x8, y8;
  wire        [15:0] a8, b8;
  partMBM #(8) dut8 (.x(x8), .y(y8), .outA(a8), .outB(b8));

  reg  signed [15:0] x16, y16;
  wire        [31:0] a16, b16;
  partMBM #(16) dut16 (.x(x16), .y(y16), .outA(a16), .outB(b16));

  integer i, j, pass, fail;
  reg signed [15:0] p8;
  reg signed [31:0] p16;
  reg [15:0] s8;
  reg [31:0] s16;
  initial begin
    pass = 0; fail = 0;
    for (i = -128; i < 128; i = i + 1)
      for (j = -128; j < 128; j = j + 1) begin
        x8 = i; y8 = j; #1;
        p8 = x8 * y8; s8 = a8 + b8;
        if (s8 === p8) pass = pass + 1;
        else begin
          if (fail < 3) $display("  8-bit mismatch x=%0d y=%0d -> %0d (expected %0d)", x8, y8, $signed(s8), p8);
          fail = fail + 1;
        end
      end
    $display("== partMBM 8-bit exhaustive signed: pass=%0d fail=%0d", pass, fail);

    pass = 0; fail = 0;
    for (i = 0; i < 20000; i = i + 1) begin
      x16 = $random; y16 = $random;
      if (i == 0) begin x16 = 16'sh8000; y16 = 16'sh8000; end
      if (i == 1) begin x16 = 16'sh7FFF; y16 = 16'sh7FFF; end
      if (i == 2) begin x16 = 16'sh8000; y16 = 16'sh7FFF; end
      if (i == 3) begin x16 = -1; y16 = -1; end
      #1;
      p16 = x16 * y16; s16 = a16 + b16;
      if (s16 === p16) pass = pass + 1;
      else begin
        if (fail < 3) $display("  16-bit mismatch x=%0d y=%0d -> %0d (expected %0d)", x16, y16, $signed(s16), p16);
        fail = fail + 1;
      end
    end
    $display("== partMBM 16-bit random signed: pass=%0d fail=%0d", pass, fail);
    $finish;
  end
endmodule
