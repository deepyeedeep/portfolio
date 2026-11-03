import cv2
import picamera2
import tflite_runtime.interpreter as tflite
import time
from collections import deque, Counter
import threading
import numpy as np
import pathlib
import time

class InferencingCamera:
    def __init__(
        self,
        model_path: pathlib.Path,
        result_window_size: int = 20
    ):
        self.isInterpreterRunning = False
        self._interpreter = tflite.Interpreter(
            model_path = str(model_path),
            experimental_delegates = [ 
                tflite.load_delegate(
                    'libedgetpu.so.1'
                )
            ]
        )
        self._interpreter_input_details = self._interpreter.get_input_details()
        self.img_size = (
            self._interpreter_input_details[0]['shape'][2],
            self._interpreter_input_details[0]['shape'][1]
        )
        # epoch, height_size, width_size, channel
        self.camera = picamera2.Picamera2()
        self.camera.configure(
            self.camera.create_video_configuration(
                main = {
                    "format": 'RGB888',
                    "size": self.img_size
                }
            )
        )
        
        self.interpreter_output_details = self._interpreter.get_output_details()
        
        # Thread or Camera buffer control signal
        self.isCameraRunning = False
        self.isInterpreterRunning = False

        # Inferencing thread
        self.inference_thread = threading.Thread(
            target = self._inferencing_loop,
            daemon = True
        )

        # Set output buffer
        self.inference_result = deque([],  maxlen = result_window_size)
        self.raw_result = { # 아무 의미 없는 값
            "latency": 88888,
            "result": 8,
            "raw_value": [888, 888, 888]
        }

    # __init__() END
    
    def run_camera(self):
        if not self.isCameraRunning: 
            self.isCameraRunning = True
            self.camera.start()
            self._interpreter.allocate_tensors()
    # run_camera() END

    def _inferencing_loop(self):
        while self.isInterpreterRunning and self.isCameraRunning:
            before_inference_time = time.time()
            img = self.camera.capture_array()
            img = cv2.cvtColor(src = img, code = cv2.COLOR_BGR2RGB)
            self._interpreter.set_tensor(
                tensor_index = self._interpreter_input_details[0]['index'],
                value = np.array([img])
            )
            self._interpreter.invoke()
            value = self._interpreter.get_tensor(self.interpreter_output_details[0]['index']).squeeze()
            self.raw_result["latency"] = round((time.time() - before_inference_time) * 1e+3)
            self.raw_result["result"] = int(np.argmax(a = value))
            self.raw_result["raw_value"] = value.tolist()
            
            self.inference_result.append(self.raw_result["result"])
        print("end infer_loop")

    # _inferencing_loop() END

    def run_inference(self):
        if not self.isCameraRunning:
            self.run_camera()
        self.isInterpreterRunning = True
        self.inference_thread = threading.Thread(
            target = self._inferencing_loop,
            daemon = True
        )
        self.inference_thread.start()
    # run_inference() END

    def stop_inference(self):
        self.isInterpreterRunning = False
        time.sleep(0.1)
        self.inference_thread.join()
        self.inference_thread = None
        print("join success")
    # stop_inference() END

    def stop_camera(self):
        if self.isInterpreterRunning:
            self.stop_inference()
        if self.isCameraRunning:
            self.isCameraRunning = False
            self.camera.stop()
    # stop_camera() END

    def get_inference_result(self):
        if self.isInterpreterRunning:
            most_frequent_value, _ = Counter(self.inference_result).most_common(n = 1)[0]
            return most_frequent_value
        else:
            print(f"self.isInterpreterRunning is {self.isInterpreterRunning}")
            return None
            
        
# Preview of Class implementation
if __name__ == '__main__':
    ai_camera_obj = InferencingCamera(
        model_path = pathlib.Path('QAT_Converted_0518_2116_edgetpu.tflite'),
        result_window_size = 20
    )
    ai_camera_obj.run_camera()
    ai_camera_obj.run_inference()
    while True:
        latency = ai_camera_obj.raw_result["latency"]
        result = ai_camera_obj.raw_result["result"]
        print(f"ai_camera_obj latency: {latency}, ai_camera_obj result: {result}")
        time.sleep(0.5)