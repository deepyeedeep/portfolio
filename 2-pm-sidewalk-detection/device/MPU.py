import smbus
import threading
from time import sleep
from collections import deque


'''
MPU6050 REGISTER ADDRESS
'''
ADDR_PWR_MGMT_1   = 0x6B
ADDR_SMPLRT_DIV   = 0x19
ADDR_CONFIG       = 0x1A
ADDR_GYRO_CONFIG  = 0x1B
ADDR_INT_ENABLE   = 0x38

# 8Bit value addr. and you need read 2 ADDR, ADDR_* and ADDR_* +1 
ADDR_ACCEL_XOUT = 0x3B
ADDR_ACCEL_YOUT = 0x3D
ADDR_ACCEL_ZOUT = 0x3F

ADDR_GYRO_XOUT  = 0x43
ADDR_GYRO_YOUT  = 0x45
ADDR_GYRO_ZOUT  = 0x47


'''
MPU6050 Object implementation
'''
# class ThreadRunSignal: # object for thread control
#     run = True

class MPU6050():
    def __init__(
            self, 
            bus = smbus.SMBus(1), DEVICE_ADDR = 0x68, 
            calibration = False, calibration_window_size = 30,
            deque_window_size = 30,
            deque_update_timer = 1e-2
    ):
        self.bus = bus
        self.DEVICE_ADDR = DEVICE_ADDR

        '''
        INIT. BLK.
        1. write to sample rate register
        2. Write to power management register
        3. Write to Configuration register
        4. Write to Gyro configuration register
        5. Write to interrupt enable register
        '''
        bus.write_byte_data(self.DEVICE_ADDR, ADDR_SMPLRT_DIV, 7)
        bus.write_byte_data(self.DEVICE_ADDR, ADDR_PWR_MGMT_1, 1)
        bus.write_byte_data(self.DEVICE_ADDR, ADDR_CONFIG, 0)
        bus.write_byte_data(self.DEVICE_ADDR, ADDR_GYRO_CONFIG, 24)
        bus.write_byte_data(self.DEVICE_ADDR, ADDR_INT_ENABLE, 1)
        
        '''
        RAW VALUE and FIFO BUFFER
        '''
        self.acc_deque = {
            f"{ADDR_ACCEL_XOUT}": deque([], maxlen = deque_window_size), # Ax Buffer
            f"{ADDR_ACCEL_YOUT}": deque([], maxlen = deque_window_size), # Ay Buffer
            f"{ADDR_ACCEL_ZOUT}": deque([], maxlen = deque_window_size), # Az Buffer
        }
        self.acc_value = {
            f"{ADDR_ACCEL_XOUT}": 0,
            f"{ADDR_ACCEL_YOUT}": 0,
            f"{ADDR_ACCEL_ZOUT}": 0
        }

        self.i2c_reader_run_signal = True
        self.deque_update_timer = deque_update_timer
        # deque update thread control signal
        
        self.i2c_sensor_reader = threading.Thread(
            target = self._read_value,
            daemon = True # main thread와 함께 움직이기 위해 주어야 함.
        )

        self.run_calibration(calibration, calibration_window_size)

        self.i2c_sensor_reader.start()
    # __init__() END

    def run_calibration(self, calibration = False, calibration_window_size = 30):
        if calibration:
            calibration_queue = {
                f"{ADDR_ACCEL_XOUT}": [], # Ax list
                f"{ADDR_ACCEL_YOUT}": [], # Ay list
                f"{ADDR_ACCEL_ZOUT}": [], # Az list
            }
            for _ in range(0, calibration_window_size):
                for register_idx in [
                    ADDR_ACCEL_XOUT, ADDR_ACCEL_YOUT, ADDR_ACCEL_ZOUT]:

                    acc_reg_value = (
                        (self.bus.read_byte_data(self.DEVICE_ADDR, register_idx) << 8) | self.bus.read_byte_data(self.DEVICE_ADDR, register_idx+1)
                    )
                    if(acc_reg_value > 32768):
                        acc_reg_value = acc_reg_value - 65536
                    calibration_queue[f"{register_idx}"].append(acc_reg_value)
                sleep(0.01)

            self.acc_biased_value = (
                sum(calibration_queue[f"{ADDR_ACCEL_XOUT}"])/len(calibration_queue[f"{ADDR_ACCEL_XOUT}"]) /1670.7115616013705055778761242429,
                sum(calibration_queue[f"{ADDR_ACCEL_YOUT}"])/len(calibration_queue[f"{ADDR_ACCEL_YOUT}"]) /1670.7115616013705055778761242429,
                sum(calibration_queue[f"{ADDR_ACCEL_ZOUT}"])/len(calibration_queue[f"{ADDR_ACCEL_ZOUT}"]) /1670.7115616013705055778761242429
            )
        else:
            # pre-calibrated value
            self.acc_biased_value = (0.048283691*9.8066, -0.029407227*9.8066, 0.161416097*9.8066)
        print(self.acc_biased_value)
        
        return self.acc_biased_value
    # get_calibration_value() END
    
    def _read_value(self): 
    # private func. cuz it has inf. loop
        while self.i2c_reader_run_signal:
            for register_idx in [ # make usable value from register value
                ADDR_ACCEL_XOUT, ADDR_ACCEL_YOUT, ADDR_ACCEL_ZOUT
            ]:
                acc_reg_value = (
                    (self.bus.read_byte_data(self.DEVICE_ADDR, register_idx) << 8) | self.bus.read_byte_data(self.DEVICE_ADDR, register_idx+1)
                )
                if(acc_reg_value > 32768):
                    acc_reg_value = acc_reg_value - 65536

                self.acc_value[f"{register_idx}"] = acc_reg_value
                self.acc_deque[f"{register_idx}"].append(acc_reg_value)
            sleep(self.deque_update_timer)

        return (
            self.acc_value[f"{ADDR_ACCEL_XOUT}"], 
            self.acc_value[f"{ADDR_ACCEL_YOUT}"], 
            self.acc_value[f"{ADDR_ACCEL_ZOUT}"]
        )
    # _read_value() END

    def get_value(self, raw_value = False):
        if raw_value:
            return (
            self.acc_value[f"{ADDR_ACCEL_XOUT}"] /1670.7115616013705055778761242429, 
            self.acc_value[f"{ADDR_ACCEL_YOUT}"] /1670.7115616013705055778761242429, 
            self.acc_value[f"{ADDR_ACCEL_ZOUT}"] /1670.7115616013705055778761242429
        )
        else:
            return (
                sum(self.acc_deque[f"{ADDR_ACCEL_XOUT}"])/len(self.acc_deque[f"{ADDR_ACCEL_XOUT}"]) /1670.7115616013705055778761242429,
                sum(self.acc_deque[f"{ADDR_ACCEL_YOUT}"])/len(self.acc_deque[f"{ADDR_ACCEL_YOUT}"]) /1670.7115616013705055778761242429,
                sum(self.acc_deque[f"{ADDR_ACCEL_ZOUT}"])/len(self.acc_deque[f"{ADDR_ACCEL_ZOUT}"]) /1670.7115616013705055778761242429,
            )
    # get_value() END
    def start_i2c_reader(self):
        self.i2c_reader_run_signal = True
        self.i2c_sensor_reader.start()

    def stop_i2c_reader(self):
        self.i2c_reader_run_signal = False # STOP self.i2c_sensor_reader
        self.i2c_sensor_reader.join() # wait for stop self.i2c_sensor_reader


# Preview of Class implementation
if __name__ == '__main__':
    gyro_obj = MPU6050(
        calibration = False,
        deque_update_timer = 0.05
    )
    axb, ayb, azb = gyro_obj.acc_biased_value
    sleep(0.1) # deque에 입력 대기 시간
    while True:
        ax, ay, az = gyro_obj.get_value(raw_value = False)
        print("Ax= %.2f m/s2" %(ax-axb), "\tAy= %.2f m/s2" %(ay-ayb), "\tAz= %.2f m/s2\t" %(az-azb), end = '\r')