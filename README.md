# 안홍재 — Embedded Linux · Edge AI Portfolio

**임베디드 리눅스 · 엣지 AI 배포 · 센서/무선 연동.**
학습된 모델을 NPU·TPU에 맞게 변환하고 양자화하는 일에서 시작해, 보드에서 드라이버를 맞추고 카메라·센서·BLE·앱까지
이어 붙여 실제 하드웨어 위에서 끝까지 동작시킨 기록을 모았습니다. 각 폴더의 README에 막혔던 지점과 해결 과정을 함께 남겼습니다.

## Overview

- **Edge AI Deployment**: Hailo-8L Dataflow Compiler(그래프 절단·PTQ·HEF 컴파일), Coral Edge TPU 양자화 인식 학습(QAT)
- **Embedded Linux**: Raspberry Pi 4B/5, PCIe 가속기 드라이버 DKMS 재설치와 모듈 파라미터, udev 규칙, BlueZ D-Bus GATT 서버
- **Sensors & Connectivity**: CSI 카메라(picamera2), I2C 가속도 센서, BLE GATT notify, TCP
- **Mobile**: Kotlin, Jetpack Compose 기반 BLE 클라이언트 앱

---

## Projects

### 1. [Hailo-8L NPU on Raspberry Pi 5 — YOLOv8n 변환부터 카메라 추론까지](./1-hailo8l-rpi5-yolov8/)
교내 연구실에서 의뢰받은 소규모 과제를 개인 학습 기록으로 정리했습니다. PC에서 변환·양자화·컴파일을, Raspberry Pi 5에서 드라이버와 추론을 직접 진행했습니다:
- Hailo가 레이어로 지원하지 않는 NMS 앞 출력 노드 6개에서 파싱을 끊고, 최적화 단계에서 `nms_postprocess`로 다시 붙여 HEF로 컴파일
- COCO2017 1,000장으로 PTQ 보정, 입력 정규화를 모델 안에 넣어 보정 데이터와 카메라 프레임이 같은 경로(RGB 그대로)를 타도록 정리
- OS에 미리 들어 있던 드라이버를 제거하고 HailoRT 버전에 맞춘 DKMS 드라이버로 재설치, `lspci`·`lsmod`·`dmesg`로 장치 인식과 펌웨어 로드 확인
- 트랜스포머(DETR)와 ONNX opset 조합에서 막힌 경로까지 [실험 기록](./1-hailo8l-rpi5-yolov8/experiments/README.md)으로 남기고, 35쪽 [안내서](./1-hailo8l-rpi5-yolov8/docs/Hailo-8L_RPi5_guide.pdf)로 재현 절차 정리

**Technologies**: Hailo Dataflow Compiler 3.28, HailoRT 4.19, Raspberry Pi 5 (PCIe M.2), DKMS, picamera2, Ultralytics YOLOv8, TFLite, Netron, WSL2, Python

### 2. [공유 킥보드 보도 주행 판별 장치 (2024 졸업작품)](./2-pm-sidewalk-detection/)
4인 팀 졸업작품, 팀장. 라즈베리파이 장치 코드, 노면 분류 모델, 안드로이드 앱을 작성했습니다:
- 킥보드 전방 카메라로 노면을 3분류(자전거도로·차도·보도)하고 Coral Edge TPU에서 추론, 최근 결과를 다수결로 묶어 프레임별 흔들림 억제
- 학습 후 정수 양자화에서 정확도가 크게 떨어져 QAT로 전환, QAT 도구가 Hardswish·어텐션을 지원하지 않아 MobileNetV2로 최종 모델 선정
- BlueZ D-Bus API를 클래스로 감싸 BLE GATT 서버를 직접 구현, 재연결 시 추론 스레드를 새로 만들도록 수정
- 앱은 BLE 노면 결과와 위치 기반 속도로 정지·보행·주행을 판단해 보도 주행과 차도 반납을 서버에 기록

**Technologies**: Raspberry Pi 4B, Coral USB Accelerator, TensorFlow/Keras, tfmot (QAT), tflite_runtime, BlueZ D-Bus, Python, Kotlin, Jetpack Compose, MVVM (ViewModel, StateFlow), Android BLE API

---

## Technical Skills

### Edge AI
- **Model Conversion**: PyTorch/Keras → ONNX/TFLite → Hailo HEF, Edge TPU 컴파일, Netron 그래프 분석
- **Quantization**: PTQ(보정 데이터셋 구성), QAT(tfmot), 양자화 지원 연산에 맞춘 모델 선택
- **Runtime**: HailoRT Python API, tflite_runtime + libedgetpu

### Embedded Linux
- **Board Bring-up**: Raspberry Pi 4B/5, PCIe 장치 인식 확인, 커널 모듈 적재·파라미터, DKMS, udev
- **Linux Userspace**: BlueZ D-Bus(GLib 메인 루프), picamera2, 가상환경과 시스템 패키지 분리

### Firmware & Interfaces
- **Languages**: C, Python, Kotlin
- **Interfaces**: I2C, BLE GATT, TCP 소켓

---

## 이 레포에 넣지 않은 것

- 재배포가 허용되지 않는 데이터셋, 벤더 배포물(SDK, 설치 파일), 학습된 모델 파일
