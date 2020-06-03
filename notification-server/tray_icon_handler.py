from os import environ

from pystray import Icon, Menu, MenuItem
from PIL import Image

def show_qr():
    print("Hello World!")
    #test_image = Image.open("test.jpg")
    #test_image.show()


def get_tray_icon():
    environ['PYNPUT_BACKEND'] = 'gtk'
    icon = Icon('Secure Notifications')
    icon.icon = Image.open("icon.png")
    menu = (
            MenuItem("Show New QR Key", show_qr),
            Menu.SEPARATOR,
            MenuItem("Quit", icon.stop),
    )

    icon.menu = menu
    return icon
    
