# BlueZ D-Bus 인터페이스 이름과 일부 UUID 정의는 Bluetooth SIG의
# 'Bluetooth for Linux Developers' 학습 자료 예제를 바탕으로 하고, 이 프로젝트용 UUID를 덧붙였다.
import dbus
import dbus.exceptions

class InvalidArgsException(dbus.exceptions.DBusException):
    _dbus_error_name = 'org.freedesktop.DBus.Error.InvalidArgs'

class NotSupportedException(dbus.exceptions.DBusException):
    _dbus_error_name = 'org.bluez.Error.NotSupported'

class NotPermittedException(dbus.exceptions.DBusException):
    _dbus_error_name = 'org.bluez.Error.NotPermitted'

class NotAuthorizedException(dbus.exceptions.DBusException):
    _dbus_error_name = 'org.bluez.Error.NotAuthorized'

class InvalidValueLengthException(dbus.exceptions.DBusException):
    _dbus_error_name = 'org.bluez.Error.InvalidValueLength'

class FailedException(dbus.exceptions.DBusException):
    _dbus_error_name = 'org.bluez.Error.Failed'

