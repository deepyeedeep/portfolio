from ble_class import bluetooth_constants, bluetooth_exceptions
from ble_class.ble_characteristic import BLECharacteristic

from typing import TypedDict
import dbus
import dbus.service

class BLEServiceConfigure(TypedDict):
    service_uuid: str
    primary: bool

class BLEService(dbus.service.Object):
    """
    implementation of dbus interface: org.bluez.GattService1 
    https://git.kernel.org/pub/scm/bluetooth/bluez.git/tree/doc/org.bluez.GattService.rst
    """
    def __init__(
            self, 
            bus: dbus.SystemBus,
            index: int, 
            uuid: str, # SERVICE UUID
            primary: bool,
            path_base = bluetooth_constants.BLUEZ_NAMESPACE/'ldsg'
    ):
        self.path = str(path_base / f"service{index}")
        self.bus = bus
        self.uuid = uuid
        self.primary = primary
        self.characteristics = []
        dbus.service.Object.__init__(self, bus, self.path)

    def get_properties(self):
        return {
            bluetooth_constants.GATT_SERVICE_INTERFACE: {
                'UUID': self.uuid,
                'Primary': self.primary,
                'Characteristics': dbus.Array(
                    self.get_characteristic_paths(),
                    signature = 'o'
                )
            }
        }
    def add_characteristic(self, characteristic):
        self.characteristics.append(characteristic)

    def get_path(self):
        return dbus.ObjectPath(self.path)

    def get_characteristic_paths(self):
        result = []
        for chrc in self.characteristics:
            result.append(chrc.get_path())
        return result

    def get_characteristics(self):
        return self.characteristics

    @dbus.service.method(
         dbus_interface = bluetooth_constants.DBUS_PROPERTIES,
         in_signature='s',
         out_signature='a{sv}'
    )
    def GetAll(self, interface):
        if interface != bluetooth_constants.GATT_SERVICE_INTERFACE:
            raise bluetooth_exceptions.bluetooth_exceptions.InvalidArgsException()

        return self.get_properties()[bluetooth_constants.GATT_SERVICE_INTERFACE]