from OpenSSL.SSL import Context, Connection, TLSv1_2_METHOD, Error
from openssl_psk import patch_context
from socket import create_server

from multiprocessing import Process
from typing import Callable
from functools import partial

import pickle
import sys

def get_secret(conn, client_identity, secrets):
    print(f"client_identity: {client_identity}")
    return secrets[client_identity]

def serve(config, handle_message : Callable[[str, str], None]) -> None:
    patch_context()

    try: 
        secrets = pickle.load(open(config.secrets_file, "rb"))
    except:
        print(f"Couldn't open secrets file '{config.secrets_file}'!")
        return

    ctx = Context(TLSv1_2_METHOD)
    ctx.set_cipher_list(b'ECDHE-PSK-CHACHA20-POLY1305')
    ctx.use_psk_identity_hint(b'our_chosen_server_identity_hint')
    partial_get_secret = partial(get_secret, secrets=secrets)
    ctx.set_psk_server_callback(partial_get_secret)

    addr = ("", config.port)
    server = Connection(ctx, create_server(addr))

    open_sockets = []

    print(f"Serving on port: {config.port}")
    try:
        while True:
            client_socket, from_addr = server.accept()
            print(f"Accepted connection from: {from_addr}")
            process = Process(target=handle_client, args=(client_socket, handle_message, ))
            process.start()
            open_sockets.append(process)

            for process in open_sockets:
                if not process.is_alive():
                    process.join()

    except KeyboardInterrupt:
        print("\nShutting down server...")
        return
    finally:
        for process in open_sockets:
            process.join()


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
            print("Socket closed")
            return
        except:
            print("Unexpected error: ", sys.exc_info()[0])
            print("Socket closed")
            return

