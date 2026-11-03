# 04. 카메라 프레임을 Hailo-8L로 연속 추론하며 FPS와 지연시간 측정
# Raspberry Pi 5의 hailo_env 가상환경(--system-site-packages)에서 실행합니다.
#
# picamera2로 640x640 RGB 프레임을 받아 HailoRT의 InferVStreams로 추론하고,
# 프레임마다 추론 시간과 FPS를 같은 줄에 갱신해 출력합니다. q를 누르면 종료합니다.
# 입력 정규화는 컴파일 단계에서 모델 안에 넣었으므로 RGB 값을 그대로 넣습니다.
import timeit

import cv2
import numpy as np
from hailo_platform import (
    HEF, VDevice, HailoStreamInterface,
    ConfigureParams, InputVStreamParams, OutputVStreamParams,
    InferVStreams, FormatType
)
from libcamera import controls
from picamera2 import Picamera2


# FPS를 같은 줄에 덮어써서 출력
def print_overwrite(message):
    print(f"\r{message.ljust(50)}", end="")


# Hailo model configuration
hef_path = './yolov8n_float32.hef'
hef = HEF(hef_path)
configure_params = ConfigureParams.create_from_hef(
    hef=hef,
    interface=HailoStreamInterface.PCIe
)

# Camera configuration
picam = Picamera2()
picam.configure(
    picam.create_video_configuration(
        main={
            "format": 'RGB888',
            'size': (640, 640)
        }
    )
)

picam.start()
picam.set_controls({"AfMode": controls.AfModeEnum.Continuous})

with VDevice() as target:  # 블록을 벗어나면 장치 자원을 해제
    network_groups = target.configure(hef, configure_params)
    network_group = network_groups[0]
    network_group_params = network_group.create_params()
    input_vstream_params = InputVStreamParams.make(
        network_group,
        format_type=FormatType.FLOAT32
    )
    output_vstream_params = OutputVStreamParams.make(
        network_group,
        format_type=FormatType.FLOAT32
    )
    input_vstream_info = hef.get_input_stream_infos()

    with InferVStreams(network_group, input_vstream_params, output_vstream_params) as infer_pipeline:
        input_layer_info = input_vstream_info[0]
        with network_group.activate(network_group_params=network_group_params):
            while True:
                start_time = timeit.default_timer()

                img = cv2.resize(
                    src=picam.capture_array(),
                    dsize=(640, 640),
                    interpolation=cv2.INTER_LINEAR
                )
                input_data = {
                    input_layer_info.name: np.expand_dims(
                        img.astype(np.float32),
                        axis=0
                    )
                }
                infer_results = infer_pipeline.infer(input_data)
                time_gap = timeit.default_timer() - start_time
                print_overwrite(
                    message=f"FPS: {round(1. / time_gap, 3)}, Delay: {round(time_gap * 1000, 3)} ms"
                )
                cv2.imshow("test", img)

                if ord('q') == cv2.waitKey(1):  # 1 ms 대기 중 q 입력 시 종료
                    break
cv2.destroyAllWindows()
