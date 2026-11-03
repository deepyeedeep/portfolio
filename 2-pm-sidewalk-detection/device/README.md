# device: 라즈베리파이 장치 코드

Raspberry Pi 4B에서 카메라 영상을 Coral Edge TPU로 추론하고, 그 결과를 BLE GATT notify로 안드로이드 앱에 보냅니다.

| 파일 | 내용 |
|---|---|
| `kickboard_ble_app.py` | 메인. BLE 광고·GATT 서버를 띄우고, 앱이 연결되면 카메라와 추론을 시작해 500 ms마다 결과를 notify. 연결이 끊기면 추론을 멈추고 다시 광고 |
| `inferencing_camera.py` | picamera2로 프레임을 받아 Edge TPU에서 추론하는 클래스. 추론은 별도 스레드에서 돌고, 최근 결과를 deque에 쌓아 다수결로 판정 |
| `ble_class/` | BlueZ D-Bus API를 감싼 클래스: `ble_advertisement.py`, `ble_application.py`, `ble_service.py`, `ble_characteristic.py` |
| `MPU.py` | MPU6050 가속도 센서 I2C 드라이버. 자이로 기능을 범위에서 빼면서 최종 버전에서는 쓰지 않음 |

## notify 데이터 (예시)

```json
{"result": 2, "reason": [0.01, 0.03, 0.96]}
```

- `result`: 최근 추론 결과의 다수결 (0 자전거도로, 1 차도, 2 보도)
- `reason`: 마지막 프레임의 모델 출력값

## 실행 환경

- Raspberry Pi 4B, Raspberry Pi OS, 카메라(CSI), Coral USB Accelerator
- Python 패키지: `tflite_runtime`(Edge TPU 런타임 포함), `picamera2`, `opencv-python`, `dbus-python`, `PyGObject`, `numpy`
- 모델 파일 `QAT_Converted_0518_2116_edgetpu.tflite`를 같은 폴더에 둡니다 (레포에는 없음).

```bash
python kickboard_ble_app.py
```

## 원본에서 바꾼 것

- 연결이 끊길 때 `stop_camera`가 괄호 없이 적혀 있어 실제로 호출되지 않던 부분을 `stop_camera()`로 고쳤습니다.
- `ble_class/__init__.py`에서 쓰지 않는 학습 자료 예제 모듈(`bluetooth_gatt`) import를 뺐습니다.
