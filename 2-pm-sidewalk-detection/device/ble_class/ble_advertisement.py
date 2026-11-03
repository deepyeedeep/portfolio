from ble_class import bluetooth_constants, bluetooth_exceptions

import dbus
import dbus.exceptions
import dbus.service
import dbus.proxies

from typing import Optional, Callable
from typing import TypedDict

from gi.repository import GLib

class AdvertisementConfigure(TypedDict):
    local_name: str
    adv_type: str


class Advertisement(dbus.service.Object):
    """
    implementation of advertising property
    Reference: https://git.kernel.org/pub/scm/bluetooth/bluez.git/tree/doc/org.bluez.LEAdvertisement.rst
    """
    def __init__(
            self, 
            bus: dbus.SystemBus, 
            index: int, # advertisement number
            local_name: str, # device_name
            advertising_type: str = 'peripheral', # central, ...
    ):
        self.path = str(bluetooth_constants.BLUEZ_NAMESPACE/'ldsg'/f'advertisement{index}')
        self.bus = bus
        self.ad_type = advertising_type
        self.service_uuids = None
        self.manufacturer_data = None
        self.solicit_uuids = None
        self.service_data = None
        self.local_name = local_name
        self.include_tx_power = False
        self.data = None
        self.discoverable = True
        dbus.service.Object.__init__(self, bus, self.path)
    # __init__() END, dbus.service.Object.path = /org/bluez/ldsg/advertisement{index}

    def get_properties(self):
        properties = dict()
        properties['Type'] = self.ad_type
        if self.service_uuids is not None:
            properties['ServiceUUIDs'] = dbus.Array(self.service_uuids,
                                                    signature='s')
        if self.solicit_uuids is not None:
            properties['SolicitUUIDs'] = dbus.Array(self.solicit_uuids,
                                                    signature='s')
        if self.manufacturer_data is not None:
            properties['ManufacturerData'] = dbus.Dictionary(
                self.manufacturer_data, signature='qv')
        if self.service_data is not None:
            properties['ServiceData'] = dbus.Dictionary(self.service_data,
                                                        signature='sv')
        if self.local_name is not None:
            properties['LocalName'] = dbus.String(self.local_name)
        if self.discoverable is not None and self.discoverable == True:
            properties['Discoverable'] = dbus.Boolean(self.discoverable)
        if self.include_tx_power:
            properties['Includes'] = dbus.Array(["tx-power"], signature='s')

        if self.data is not None:
            properties['Data'] = dbus.Dictionary(
                self.data, signature='yv')
        return {bluetooth_constants.ADVERTISING_MANAGER_INTERFACE: properties} # Condition 1
    # get_properties() END

    def get_path(self):
        return dbus.ObjectPath(self.path)

    @dbus.service.method( # decorator of GetAll()
        dbus_interface = bluetooth_constants.DBUS_PROPERTIES,
        in_signature = 's',
        out_signature = 'a{sv}'
    )
    def GetAll(self, interface):
        if interface != bluetooth_constants.ADVERTISEMENT_INTERFACE:
            raise bluetooth_exceptions.InvalidArgsException()
        return self.get_properties()[bluetooth_constants.ADVERTISING_MANAGER_INTERFACE]

    @dbus.service.method( # decorator of Release()
            bluetooth_constants.ADVERTISING_MANAGER_INTERFACE,
            in_signature='',
            out_signature=''
    )
    def Release(self):
        print('%s: Released' % self.path)

# class Advertisement END

class AdvertisementManager:
    """
    wrapper object of dbus interface: org.bluez.LEAdvertisingManager1
    Reference: https://git.kernel.org/pub/scm/bluetooth/bluez.git/tree/doc/org.bluez.LEAdvertisingManager.rst
    """
    def __init__(
        self,
        bus: dbus.SystemBus,
        advertisement: Advertisement,
        dbus_name = bluetooth_constants.BLUEZ_SERVICE_NAME
    ):
        self.adv_mgr_interface = dbus.Interface(
            object = bus.get_object(
                bus_name = dbus_name,
                object_path = str(bluetooth_constants.BLUEZ_NAMESPACE / bluetooth_constants.ADAPTER_NAME)
            ),
            dbus_interface = bluetooth_constants.ADVERTISING_MANAGER_INTERFACE
        )
        self.advertisement = advertisement
        self.isRegistered = False

    # __init__() END

    def register_advertisement(self):
        if not self.isRegistered:
            self.adv_mgr_interface.RegisterAdvertisement(
                self.advertisement.get_path(),
                {},
                reply_handler = self.onAdvertisementRegistered,
                error_handler = self.onAdvertisementRegisterFailed
            )
            self.isRegistered = True
        else:
            print("Advertisement already registered. aborted.")

    # register_advertisement() END

    def unregister_advertisement(self):
        if self.isRegistered:
            self.adv_mgr_interface.UnregisterAdvertisement(self.advertisement.get_path())
            self.isRegistered = False
        else:
            print("Registered advertisement not exist. aborted.")

    def onAdvertisementRegistered(self):
        print('Advertisement registered OK')

    def onAdvertisementRegisterFailed(self, error):
        print('Error: Failed to register advertisement: ' + str(error))
        # mainloop.quit()
