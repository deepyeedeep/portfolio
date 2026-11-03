# model: 노면 분류 모델 (정리 중)

킥보드 전방 영상을 자전거도로·차도·보도 3가지로 분류하는 모델입니다. 학습 코드는 원본을 정리한 뒤 이 폴더에 추가할 예정입니다.

## 장치에 올린 모델의 학습 설정 (2024-05-18 21:16 실행)

| 항목 | 값 |
|---|---|
| 모델 | MobileNetV2 (alpha 1.0, expansion 1.0) |
| 입력 | 288 × 512 RGB |
| 학습 | batch 8, learning rate 1e-5, 20 epochs, `val_categorical_accuracy` 기준 저장 |
| 양자화 인식 학습 | 8 epochs (learning rate 배율 500) |
| 변환 | 정수 양자화 TFLite → Edge TPU 컴파일 |

## 구성 (추가 예정)

| 파일 | 내용 |
|---|---|
| `main.py` | 학습 진입점 |
| `ped_road_classify_dataset.py` | tf.data 데이터 로더, Albumentations 증강 (MotionBlur 포함) |
| `blocks.py`, `Mobilenet.py`, `MobileViT.py` | 모델 구성 블록과 MobileNet 계열, MobileViT 구현 |
| `quantize.py`, `QATandQuantized.py` | 양자화 인식 학습(tfmot)과 TFLite 변환 |
| `utils.py` | 실행별 기록 폴더 정리 |

MobileNetV3와 MobileViT도 구현해 시도했지만, 양자화 인식 학습 도구가 Hardswish와 어텐션 구조를 지원하지 않아 MobileNetV2를 최종 모델로 정했습니다.
