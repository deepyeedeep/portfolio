# Developed class
from ble_class.ble_advertisement import AdvertisementManager, Advertisement
from ble_class.ble_advertisement import AdvertisementConfigure
from ble_class.ble_application import BLEApplication, DBusApplicationHandle
from ble_class.ble_service import BLEService
from ble_class.ble_characteristic import BLECharacteristic
from ble_class import bluetooth_constants
from MPU import MPU6050
from inferencing_camera import InferencingCamera

# BLUEZ ACCESS PKG 
import dbus
import dbus.mainloop.glib
import dbus.exceptions
from gi.repository import GLib
import RPi.GPIO as GPIO

import threading
import json
from typing import List, Callable
import pathlib

'''
BLUEZ with dbus 헷갈리는 경우 아래의 git document 참조
https://git.kernel.org/pub/scm/bluetooth/bluez.git/tree/doc
'''
class KickboardGattServerApp:
    def __init__(
        self, advertisement_configure: AdvertisementConfigure
    ) -> None:
        dbus.mainloop.glib.DBusGMainLoop(set_as_default=True)
        self.mainloop = GLib.MainLoop()
        self.mainloop_thread = threading.Thread(
            target = self.mainloop.run,
            daemon = True
        )

        self.bus = dbus.SystemBus()

        self.adv_mgr = AdvertisementManager(
            bus = self.bus,
            advertisement = Advertisement(
                bus = self.bus, index = 0, local_name = advertisement_configure["local_name"],
                advertising_type = advertisement_configure["adv_type"],
            ),
        )
        self.gatt_service = BLEService(
            bus = self.bus, index = 0,
            uuid = bluetooth_constants.KICK_SVC_UUID,
            primary = True
        )
        self.gatt_characteristic = BLECharacteristic(
            self.bus, index = 0,
            uuid = bluetooth_constants.KICK_STR_CHR_UUID,
            flags = ['notify'],
            service = self.gatt_service
        )
        self.gatt_service.add_characteristic(self.gatt_characteristic)
        self.dbus_app = BLEApplication(bus = self.bus, services = [self.gatt_service])

        # Sensor Initialize
        self.predictor = InferencingCamera(
            pathlib.Path('QAT_Converted_0518_2116_edgetpu.tflite'),
            result_window_size = 1000
        )
        # self.imu_sensor = MPU6050(
        #     calibration = False,
        #     deque_update_timer = 0.05
        # )

        self.glib_event_id = 0

        self.bus.add_signal_receiver(
            self.onPropertiesChanged,
            dbus_interface = bluetooth_constants.DBUS_PROPERTIES,
            signal_name = "PropertiesChanged",
            path_keyword = "path"
        )
        self.bus.add_signal_receiver(
            self.onInterfacesAdded, #
            dbus_interface = bluetooth_constants.DBUS_OM_IFACE, # dbus obj. manager
            signal_name = "InterfacesAdded"
        )
    # __init__() END

    def run(self):
        self.adv_mgr.register_advertisement()
        self.mainloop_thread.start()

    def quit(self):
        self.adv_mgr.unregister_advertisement()
        self.mainloop.quit()
        self.mainloop_thread.join()

    def onPropertiesChanged(self, interface, changed, invalidated, path):
        if (interface == bluetooth_constants.DEVICE_INTERFACE):
            if ("Connected" in changed):
                self.onChangedConnectStatus(changed["Connected"])

    def onInterfacesAdded(self, path, interfaces):
        if bluetooth_constants.DEVICE_INTERFACE in interfaces:
            properties = interfaces[bluetooth_constants.DEVICE_INTERFACE]
            if ("Connected" in properties):
                self.onChangedConnectStatus(properties["Connected"])

    def onChangedConnectStatus(self, status):
        if status == 1: # if connected
            print("Connected")
            self.adv_mgr.unregister_advertisement()

            # Camera and Inference control
            self.predictor.run_camera()
            self.predictor.run_inference()

            # Notify values by 500 ms
            self.glib_event_id = GLib.timeout_add(500, self.update_characteristic_value)

        else: # if disconnected
            print("Disconnected")
            if self.glib_event_id:
                print(self.glib_event_id)
                GLib.source_remove(self.glib_event_id)
            self.adv_mgr.register_advertisement()

            self.predictor.stop_inference()
            self.predictor.stop_camera()

    def update_characteristic_value(self):
        # Code for update characteristic values
        reason = self.predictor.raw_result["raw_value"]
        result = self.predictor.get_inference_result()


        if self.gatt_characteristic.isNotifying:
            value = dbus.ByteArray(
                bytes(
                    json.dumps({
                        "result": result,
                        "reason": reason
                    }),
                    encoding = 'utf-8'
                )
            )
            self.gatt_characteristic.PropertiesChanged(bluetooth_constants.GATT_CHARACTERISTIC_INTERFACE, { 'Value': value }, [])

        return True  # GLib.timeout_add requires the callback to return True to continue calling
    # update_characteristic_value() END


if __name__ == "__main__":
    app = KickboardGattServerApp(
        advertisement_configure = {
            "local_name": '123456',
            "adv_type": 'peripheral'
        }
    )
    app.run()
    while 'q' != input("ENTER q IF YOU WANT QUIT APP."):
        pass
    app.quit()