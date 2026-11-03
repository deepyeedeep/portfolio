# 실험 기록: 트랜스포머 계열 모델(DETR)과 ONNX 경로

최종 결과물은 YOLOv8n + TFLite 경로이지만, 그 전에 시도했다가 막힌 경로를 기록해 둡니다. 다른 사람의 코드를 그대로 옮긴 노트북은 이 레포에 넣지 않고 원본 링크로 대신합니다.

## 1. DETR (ResNet-50 백본) 파싱 시도

처음에는 트랜스포머 기반 객체 검출 모델인 DETR을 Hailo-8L에 올리려고 했습니다.

| 날짜 | 시도 | 결과 |
|---|---|---|
| 2024-08-18 | 사전학습된 DETR(ResNet-50)을 ONNX로 내보내 `translate_onnx_model`로 파싱 | 단순화한 모델로 재시도 (로그에 완료 기록 없음) |
| 2024-08-19 | DETR 데모 모델을 ONNX로 내보내 반복 파싱 (원본, 단순화, 상수 접기 버전) | ONNX shape inference에서 Add 노드 브로드캐스트 오류 경고, 여러 차례 재시도 |
| 2024-08-22 | [ai-edge-torch](https://github.com/google-ai-edge/ai-edge-torch)로 NHWC 입력 TFLite로 변환해 `translate_tf_model`로 파싱 | TFLite 변환까지 진행 (파싱 결과 기록 없음) |

파싱 로그에서 확인한 메시지:

```
WARNING - parser.py:141 - Large model detected. The graph may contain either a large number of operators,
          or weight variables with a very large capacity.
WARNING - parser.py:298 - ONNX shape inference failed: ... Non-zero status code returned while running Add node.
          Name:'/Add' ... Attempting to broadcast an axis by a dimension other than 1. 950 by 1050
WARNING - translator.py:121 - This model has Batch normalization layers exported in training mode, ...
```

직접 변환한 DETR 모델로는 컴파일된 결과물(HEF)까지 가지 못했고, 대상 모델을 YOLOv8n으로 바꿨습니다.

- DETR 원본: https://github.com/facebookresearch/detr

## 2. ONNX opset 조합 탐색

YOLO 계열(v5~v11)과 RT-DETR을 ONNX로 내보내면서 opset 8, 11~17을 바꿔 가며 Dataflow Compiler가 파싱할 수 있는 조합을 찾으려 했습니다. YOLOv10n(opset 11)을 ONNX 노드 이름 기준으로 NMS 앞에서 잘라 파싱하는 것도 시도했습니다. 최종적으로는 opset 문제가 없는 SavedModel/TFLite 경로를 택했습니다.
