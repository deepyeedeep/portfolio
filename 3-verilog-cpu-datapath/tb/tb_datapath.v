`timescale 1ns/1ps
// Self-checking testbench for the 2019 datapath modules (written in 2026)
module tb_datapath;
  localparam BW = 16;
  reg  [BW-1:0] A, B;
  reg  [3:0]    GS;
  wire [BW-1:0] G;
  wire          Cout, CCout;
  reg  [BW:0]   ref;   // 17-bit reference including carry
  integer i, op, pass, fail, total_fail;

  ALU #(BW) dut_alu (.A(A), .B(B), .GS(GS), .Cout(Cout), .CCout(CCout), .ALU_Result(G));

  // shifter under test
  reg  [1:0]    HS;
  wire [BW-1:0] H;
  Shifter #(BW) dut_sh (.busB(B), .IR(1'b0), .IL(1'b0), .HS(HS), .Out(H));

  // function unit flags
  reg  [4:0]    FS;
  wire [3:0]    VCNZ;
  wire [BW-1:0] F;
  Funit #(BW) dut_fu (.busA(A), .busB(B), .FS(FS), .VCNZ(VCNZ), .Fout(F));

  function [BW:0] alu_ref(input [3:0] gs, input [BW-1:0] a, input [BW-1:0] b);
    case (gs)
      4'b0000: alu_ref = {1'b0,a} + 0;
      4'b0001: alu_ref = {1'b0,a} + 1;
      4'b0010: alu_ref = {1'b0,a} + {1'b0,b};
      4'b0011: alu_ref = {1'b0,a} + {1'b0,b} + 1;
      4'b0100: alu_ref = {1'b0,a} + {1'b0,~b};
      4'b0101: alu_ref = {1'b0,a} + {1'b0,~b} + 1;
      4'b0110: alu_ref = {1'b0,a} + {1'b0,{BW{1'b1}}};
      4'b0111: alu_ref = {1'b0,a} + {1'b0,{BW{1'b1}}} + 1;
      4'b1000, 4'b1001: alu_ref = {1'b0, a & b};
      4'b1010, 4'b1011: alu_ref = {1'b0, a | b};
      4'b1100, 4'b1101: alu_ref = {1'b0, a ^ b};
      default:          alu_ref = {1'b0, ~a};
    endcase
  endfunction

  initial begin
    total_fail = 0;
    $display("== ALU: 16 operations x 2000 random vectors ==");
    for (op = 0; op < 16; op = op + 1) begin
      pass = 0; fail = 0; GS = op[3:0];
      for (i = 0; i < 2000; i = i + 1) begin
        A = $random; B = $random;
        if (i == 0) begin A = 16'hFFFF; B = 16'h0001; end
        if (i == 1) begin A = 16'h7FFF; B = 16'h0001; end
        if (i == 2) begin A = 16'h0000; B = 16'h0000; end
        #1;
        ref = alu_ref(GS, A, B);
        if (G === ref[BW-1:0] && (GS[3] || Cout === ref[BW])) pass = pass + 1;
        else begin
          if (fail < 2) $display("   mismatch GS=%b A=%h B=%h -> G=%h C=%b (expected G=%h C=%b)", GS, A, B, G, Cout, ref[BW-1:0], ref[BW]);
          fail = fail + 1;
        end
      end
      total_fail = total_fail + fail;
      $display("  GS=%b  pass=%0d fail=%0d", GS, pass, fail);
    end

    $display("== Funit flags (V, C, N, Z) on A+B and A-B ==");
    pass = 0; fail = 0;
    for (i = 0; i < 4000; i = i + 1) begin
      A = $random; B = $random;
      if (i == 0) begin A = 16'h7FFF; B = 16'h0001; end
      if (i == 1) begin A = 16'h8000; B = 16'h0001; end
      if (i == 2) begin A = 16'h1234; B = 16'h1234; end
      FS = (i % 2) ? 5'b00101 : 5'b00010;
      #1;
      ref = alu_ref(FS[3:0], A, B);
      if (VCNZ[2] === ref[BW] && VCNZ[1] === ref[BW-1] && VCNZ[0] === (ref[BW-1:0] == 0) &&
          VCNZ[3] === ((FS[3:0] == 4'b0010) ? (A[BW-1] == B[BW-1] && ref[BW-1] != A[BW-1])
                                            : (A[BW-1] != B[BW-1] && ref[BW-1] != A[BW-1])))
        pass = pass + 1;
      else begin
        if (fail < 2) $display("   mismatch FS=%b A=%h B=%h VCNZ=%b", FS, A, B, VCNZ);
        fail = fail + 1;
      end
    end
    total_fail = total_fail + fail;
    $display("  flags  pass=%0d fail=%0d", pass, fail);

    $display("== Shifter (expected: 01 = logical right, 10 = logical left) ==");
    for (op = 0; op < 3; op = op + 1) begin
      pass = 0; fail = 0; HS = op[1:0];
      for (i = 0; i < 500; i = i + 1) begin
        B = $random; #1;
        if ((HS == 2'b00 && H === B) || (HS == 2'b01 && H === (B >> 1)) || (HS == 2'b10 && H === (B << 1))) pass = pass + 1;
        else begin
          if (fail < 1) $display("   mismatch HS=%b B=%h -> %h (expected %h)", HS, B, H, (HS == 2'b01) ? (B >> 1) : (B << 1));
          fail = fail + 1;
        end
      end
      $display("  HS=%b  pass=%0d fail=%0d", HS, pass, fail);
    end

    $display("== ALU total arithmetic/logic failures: %0d ==", total_fail);
    $finish;
  end
endmodule
