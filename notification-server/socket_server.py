import asyncio
from typing import Callable

def serve(port : int, handle_message : Callable[[str], None]) -> None:
    print(f"Serving on port: {port}")
    loop = asyncio.get_event_loop()
    loop.create_task(asyncio.start_server(lambda reader, _: handle_client(reader, handle_message), 'localhost', port))
    try:
        loop.run_forever()
    except KeyboardInterrupt:
        print("Shutting down server...")
        return


async def handle_client(reader, handle_message : Callable[[str], None]) -> None:
    while True:
        request = await reader.readline()
        if not request:
            return
        try:
            decoded = request.decode("utf-8", "strict").strip()
            handle_message(decoded)
        except UnicodeDecodeError:
            continue


