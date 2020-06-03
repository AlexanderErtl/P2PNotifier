from OpenSSL.SSL import Context, Connection, TLSv1_2_METHOD, Error
from openssl_psk import patch_context
from socket import create_server

from threading import Thread
from typing import Callable
from functools import partial

import pickle
import sys

def get_secret(conn, client_identity, secrets):
    print(f"client_identity: {client_identity}")
    return secrets[client_identity]

def serve(socket, handle_message : Callable[[str, str], None], running : bool) -> None:
    patch_context()

    try: 
        # todo fix hardcoded secrets
        secrets = pickle.load(open("SECRETS", "rb"))
    except:
        print(f"Couldn't open secrets file '{SECRETS}'!")
        return

    ctx = Context(TLSv1_2_METHOD)
    ctx.set_cipher_list(b'ECDHE-PSK-CHACHA20-POLY1305')
    ctx.use_psk_identity_hint(b'our_chosen_server_identity_hint')
    partial_get_secret = partial(get_secret, secrets=secrets)
    ctx.set_psk_server_callback(partial_get_secret)

    server = Connection(ctx, socket)

    open_sockets = []

    addr, port = socket.getsockname()
    print(f"Serving on: {addr}:{port}")
    while running:
        try:
            client_socket, from_addr = server.accept()
        except:
            continue

        addr, port = from_addr
        print(f"Accepted connection from {addr}:{port}")
        thread = Thread(target=handle_client, args=(client_socket, handle_message, ))
        thread.start()
        open_sockets.append(thread)

        for thread in open_sockets:
            if not thread.is_alive():
                thread.join()

    for thread in open_sockets:
        thread.join()


def handle_client(socket, handle_message : Callable[[str, str], None]) -> None:
    while True:
        try:
            message = socket.read(1024)
            while socket.pending():
                message += socket.read(1024)
            decoded = message.decode("utf-8", "strict").strip()
            print("Received: ", decoded)
            handle_message("Notification Title Placeholder", decoded)
        except UnicodeDecodeError:
            print("Couldn't decode unicode message!")
            continue
        except Error as error_inst:
            print("OpenSSL Error: ", error_inst)
            addr, port = socket.getpeername()
            print(f"Socket {addr}:{port} closed")
            return
        except:
            print("Unexpected error: ", sys.exc_info()[0])
            addr, port = socket.getpeername()
            print(f"Socket {addr}:{port} closed")
            return

