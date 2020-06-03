from os import environ

from pystray import Icon, Menu, MenuItem
from PIL import Image

from typing import Callable, Tuple
from base64 import b64encode

import json
import qrcode

def show_qr(get_new_secret : Callable[[None], Tuple[bytes, bytes]]):
    identity, secret = get_new_secret()
    b64_identity = b64encode(identity).decode('utf-8')
    b64_secret = b64encode(secret).decode('utf-8')
    qr_content = {
            "identity": b64_identity,
            "secret": b64_secret,
    }
    print("Created new identity and secret: ", json.dumps(qr_content, indent=2))
    qr_img = qrcode.make(json.dumps(qr_content))
    qr_img.show()
    #test_image = Image.open("test.jpg")
    #test_image.show()


def get_tray_icon(get_new_secret):
    environ['PYNPUT_BACKEND'] = 'gtk'
    icon = Icon('Secure Notifications')
    icon.icon = Image.open("icon.png")
    menu = (
            MenuItem("Show New QR Key", lambda: show_qr(get_new_secret)),
            Menu.SEPARATOR,
            MenuItem("Quit", icon.stop),
    )

    icon.menu = menu
    return icon
    
