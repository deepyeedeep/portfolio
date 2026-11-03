from ble_class import bluetooth_gatt, bluetooth_constants, bluetooth_exceptions

import dbus
import dbus.service

class BLECharacteristic(dbus.service.Object):
    """
    implementation of dbus interface: org.bluez.GattCharacteristic1
    https://git.kernel.org/pub/scm/bluetooth/bluez.git/tree/doc/org.bluez.GattCharacteristic.rst

    """
    def __init__(self, bus, index, uuid, flags, service):
        self.path = service.path + '/char' + str(index)
        print("creating Characteristic with path="+self.path)
        self.bus = bus
        self.uuid = uuid
        self.service = service
        self.flags = flags
        self.descriptors = []
        self.isNotifying = False
        self.received_value = "NULL"
        dbus.service.Object.__init__(self, bus, self.path)

    def get_properties(self):
        return {
                bluetooth_constants.GATT_CHARACTERISTIC_INTERFACE: {
                        'Service': self.service.get_path(),
                        'UUID': self.uuid,
                        'Flags': self.flags,
                        'Descriptors': dbus.Array(
                                self.get_descriptor_paths(),
                                signature='o')
                }
        }
    
    def get_path(self):
        return dbus.ObjectPath(self.path)

    def add_descriptor(self, descriptor):
        self.descriptors.append(descriptor)

    def get_descriptor_paths(self):
        result = []
        for desc in self.descriptors:
            result.append(desc.get_path())
        return result

    def get_descriptors(self):
        return self.descriptors

    @dbus.service.method(bluetooth_constants.DBUS_PROPERTIES,
                         in_signature='s',
                         out_signature='a{sv}')
    def GetAll(self, interface):
        if interface != bluetooth_constants.GATT_CHARACTERISTIC_INTERFACE:
            raise bluetooth_exceptions.bluetooth_exceptions.InvalidArgsException()

        return self.get_properties()[bluetooth_constants.GATT_CHARACTERISTIC_INTERFACE]

    @dbus.service.method(bluetooth_constants.GATT_CHARACTERISTIC_INTERFACE,
                        in_signature='a{sv}',
                        out_signature='ay')
    def ReadValue(self, options):
        print('Default ReadValue called, returning error')
        raise bluetooth_exceptions.NotSupportedException()

    @dbus.service.method(bluetooth_constants.GATT_CHARACTERISTIC_INTERFACE, in_signature='aya{sv}')
    def WriteValue(self, value, options):
        if len(value) > 0:
            self.received_value = bytes(value).decode('utf-8')
            print(self.received_value)


    @dbus.service.method(bluetooth_constants.GATT_CHARACTERISTIC_INTERFACE)
    def StartNotify(self):
        print("START NOTIFY")
        self.isNotifying = True

    @dbus.service.method(bluetooth_constants.GATT_CHARACTERISTIC_INTERFACE)
    def StopNotify(self):
        print("STOP NOTIFY")
        self.isNotifying = False

    @dbus.service.signal(bluetooth_constants.DBUS_PROPERTIES,
                         signature='sa{sv}as')
    def PropertiesChanged(self, changed, invalidated, interface = bluetooth_constants.GATT_CHARACTERISTIC_INTERFACE):
        pass