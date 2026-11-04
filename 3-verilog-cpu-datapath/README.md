# 16비트 CPU 데이터패스: Verilog ALU · 가산기 · 레지스터 파일 (2019 디지털시스템설계)

2019년 2학기 디지털시스템설계 과목에서 팀으로 만든 마이크로프로그램 제어 CPU 가운데, 제가 작성한 **데이터패스 모듈만** 정리했습니다. 산술·논리 연산부(ALU), carry-select 가산기, 시프터, 레지스터 파일, 상태 플래그를 Verilog로 설계했고, 이 ALU는 수정 없이 팀의 최종 CPU에 들어가 DE2-115 FPGA 보드에서 동작했습니다.

2026년에 자가 검증 테스트벤치를 새로 붙여 다시 검증했습니다. ALU는 16개 연산 모두 통과했고, 시프터에서는 당시 놓친 비트 범위 버그를 찾았습니다.

> 제어부(마이크로프로그램 제어 메모리, 제어 주소 레지스터), 메모리, 보드 연결은 팀 작업이라 이 폴더에 넣지 않았습니다.

## 구성

```mermaid
flowchart LR
  RF["Rfile<br/>레지스터 R0~R7 + 임시 R8"] -- "A 버스" --> FU
  RF -- "B 버스" --> MB{"MUX B"}
  CONST["상수 입력"] --> MB
  MB --> FU
  subgraph FU["Funit · 기능 유닛"]
    direction TB
    ALU["ALU<br/>산술부 AC + 논리부 LC"] --> MF{"MUX F"}
    SH["Shifter"] --> MF
    ALU --> FLAG["V · C · N · Z 플래그"]
  end
  MF --> MD{"MUX D"}
  MEM["메모리 데이터"] --> MD
  MD -- "D 버스" --> RF
```

### ALU 내부

```mermaid
flowchart LR
  B["B"] --> SEL["B 입력 선택<br/>GS[2:1]에 따라 0 · B · ~B · 1"]
  A["A"] --> CSA["CSA 16비트<br/>4비트 블록 4개, 블록마다 캐리 0과 1을 미리 계산"]
  SEL --> CSA
  CIN["Cin = GS[0]"] --> CSA
  A --> LC["LC 논리부<br/>AND · OR · XOR · NOT"]
  B --> LC
  CSA --> M{"GS[3]로 선택"}
  LC --> M
  M --> OUT["ALU 결과"]
  CSA --> CO["캐리 출력, MSB로 들어가는 캐리"]
```

| 파일 | 내용 |
|---|---|
| [`src/HA1.v`](src/HA1.v) | 반가산기. 가산기에서 전파(P)·생성(G) 신호를 만드는 데 사용 |
| [`src/CLA.v`](src/CLA.v) | 4비트 가산기 블록 (P·G 기반) |
| [`src/CSA.v`](src/CSA.v) | carry-select 가산기. 4비트 블록마다 캐리 0과 1의 결과를 동시에 계산해 두고, 앞 블록의 캐리로 하나를 고름 |
| [`src/AC.v`](src/AC.v) | 산술부. B 입력을 0, B, ~B, 1로 바꿔 CSA에 넣어 덧셈·뺄셈·증감을 한 회로로 처리 |
| [`src/LC.v`](src/LC.v) | 논리부. AND, OR, XOR, NOT |
| [`src/ALU.v`](src/ALU.v) | 산술부와 논리부를 GS[3]로 선택 |
| [`src/Shifter.v`](src/Shifter.v) | 시프터 (통과, 오른쪽, 왼쪽) |
| [`src/Funit.v`](src/Funit.v) | 기능 유닛. ALU와 시프터를 FS[4]로 선택하고 V·C·N·Z 플래그 생성 |
| [`src/Rfile.v`](src/Rfile.v) | 레지스터 파일. 쓰기 1포트, 읽기 2포트 |
| [`src/cpu_dp.v`](src/cpu_dp.v) | 위 모듈과 버스 MUX를 묶은 데이터패스 |

### ALU 연산표 (GS)

| GS | 연산 | GS | 연산 |
|---|---|---|---|
| 0000 | A | 0110 | A − 1 |
| 0001 | A + 1 | 0111 | A |
| 0010 | A + B | 100x | A AND B |
| 0011 | A + B + 1 | 101x | A OR B |
| 0100 | A + ~B | 110x | A XOR B |
| 0101 | A − B | 111x | NOT A |

플래그는 V(오버플로: MSB로 들어가는 캐리와 나가는 캐리의 XOR), C(캐리), N(최상위 비트), Z(결과가 0)입니다.

## 검증 (2026)

[`tb/tb_datapath.v`](tb/tb_datapath.v)는 각 연산의 결과를 Verilog 기준 모델과 비교하는 자가 검증 테스트벤치입니다. 무작위 값과 경계값(0xFFFF + 1, 0x7FFF + 1 등)을 함께 넣었습니다. 실행 결과 원문은 [`sim/modelsim_result.txt`](sim/modelsim_result.txt)에 있습니다.

| 대상 | 검사 | 결과 |
|---|---|---|
| ALU | 16개 연산 × 무작위 2,000개 (결과와 캐리) | **32,000 / 32,000 통과** |
| Funit 플래그 | A + B, A − B에서 V·C·N·Z 4,000건 | **4,000 / 4,000 통과** |
| Shifter 통과 (HS=00) | 500건 | 500 / 500 통과 |
| Shifter 오른쪽 (HS=01) | 500건 | **0 / 500** (아래 버그) |
| Shifter 왼쪽 (HS=10) | 500건 | **241 / 500** (아래 버그) |

```text
ModelSim ALTERA Starter Edition 10.5b
vlib work
vlog src/HA1.v src/CLA.v src/CSA.v src/AC.v src/LC.v src/ALU.v src/Shifter.v src/Funit.v src/Rfile.v src/cpu_dp.v tb/tb_datapath.v
vsim -c tb_datapath -do "run -all; quit -f"
```

## 재검토에서 찾은 문제 (2019년 코드 그대로 둠)

| 위치 | 문제 | 고치는 방법 |
|---|---|---|
| `Shifter.v` 오른쪽 시프트 | `{IR, busB[BW-1]}`이 2비트만 만들어, 결과가 0x0001 같은 값이 됨 | `{IR, busB[BW-1:1]}` |
| `Shifter.v` 왼쪽 시프트 | `{busB[BW-3:0], IL}`이 15비트라 최상위 비트 하나가 사라짐. 그래서 절반 정도만 맞음 | `{busB[BW-2:0], IL}` |
| `cpu_dp.v` | `MM`이 선언만 되고 구동되지 않아 `Aout`이 정해지지 않음. 3비트 주소를 4비트 포트에 연결해 임시 레지스터 R8에 접근할 수 없음 | 제어 워드에 MM 비트를 추가하고 주소 폭을 맞춤 |
| `CLA.v` | 이름은 carry lookahead지만 캐리를 앞 비트에서 차례로 넘기는 식이라 실제로는 리플 구조 | 각 캐리를 P·G의 곱의 합으로 펼친 lookahead 식으로 변경 |

이 문제들은 당시 팀 통합 과정에서 최종 CPU 쪽 코드에서는 고쳐졌습니다. 이 폴더에는 제가 작성한 2019년 원본을 그대로 두고, 재검토 결과만 기록했습니다.

## 이 폴더에 넣지 않은 것

- 팀이 함께 만든 제어부, 메모리, 보드 연결 코드와 Quartus 프로젝트 파일
- 과목 강의 자료

## AI 활용

Verilog 모듈은 2019년에 직접 작성했고, 2026년 재검토 때 테스트벤치(`tb/`)와 문서 작성에 Claude Code를 활용했습니다.
