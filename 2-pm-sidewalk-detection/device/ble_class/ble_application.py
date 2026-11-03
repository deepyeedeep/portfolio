from ble_class import bluetooth_constants
from ble_class.ble_service import BLEService

import dbus
import dbus.exceptions
import dbus.service
import dbus.proxies
from gi.repository import GLib

from typing import List, Dict, TypedDict
import threading

class DBusApplicationHandle(TypedDict):
    bus: dbus.SystemBus
    mainloop: GLib.MainLoop
    mainloop_thread: threading.Thread

class BLEApplication(dbus.service.Object):
    def __init__(
            self, 
            bus: dbus.SystemBus,
            services: List[BLEService],
    ):
        self.path = '/'
        self.services = services
        self.gatt_manager = dbus.Interface(
            object = bus.get_object(
                bus_name = bluetooth_constants.BLUEZ_SERVICE_NAME, 
                object_path = str(bluetooth_constants.BLUEZ_NAMESPACE / bluetooth_constants.ADAPTER_NAME)
            ),
            dbus_interface = bluetooth_constants.GATT_MANAGER_INTERFACE
        )
        self.gatt_manager.RegisterApplication(
            self.get_path(),
            {},
            reply_handler = self.register_app_callback,
            error_handler = self.register_app_callback
        )
        dbus.service.Object.__init__(self, bus, self.path)
    # __init__() END

    def get_path(self):
        return dbus.ObjectPath(self.path)

    @dbus.service.method(
        bluetooth_constants.DBUS_OM_IFACE, 
        out_signature='a{oa{sa{sv}}}'
    ) 
    def GetManagedObjects(self):
        response = {}

        for service in self.services:
            response[service.get_path()] = service.get_properties()
            chrcs = service.get_characteristics()
            for chrc in chrcs:
                response[chrc.get_path()] = chrc.get_properties()
                descs = chrc.get_descriptors()
                for desc in descs:
                    response[desc.get_path()] = desc.get_properties()

        return response
    # GetManagedObjects() END

    def register_app_callback(self):
        print('GATT application registered')

    def register_app_error_callback(self, error):
        print('Failed to register application: ' + str(error))
        # mainloop.quit()