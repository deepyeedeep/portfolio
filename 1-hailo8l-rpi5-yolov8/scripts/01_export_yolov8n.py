# 01. YOLOv8n을 TensorFlow SavedModel/TFLite로 내보내기
# PC(WSL2 Ubuntu 22.04)의 ultralytics_env 가상환경에서 실행합니다.
#
# ONNX로도 내보낼 수 있지만 opset 버전 문제로 Dataflow Compiler 파싱이 매끄럽지 않아
# SavedModel 형식을 쓰고, 함께 생성되는 yolov8n_float32.tflite를 다음 단계에서 파싱합니다.
import pathlib
import shutil

import ultralytics

ultralytics.checks()  # ultralytics 패키지 정상 동작 확인

model = ultralytics.YOLO('yolov8n.pt')  # COCO2017로 사전학습된 모델
model.export(format='saved_model')

# 다음 단계(02 노트북)가 읽는 ./tf_model 아래로 옮긴다
pathlib.Path('./tf_model').mkdir(exist_ok=True)
shutil.move(src='./yolov8n_saved_model', dst='./tf_model/yolov8n_saved_model')
