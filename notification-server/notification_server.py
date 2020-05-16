from configargparse import ArgParser, Namespace, YAMLConfigFileParser
from sys import exit

from notification_handler import notify
from socket_server import serve

def main() -> int:
    config = parse_config()
    serve(config.port, handle_msg)
    return 0

def handle_msg(msg : str) -> None:
    notify("New notification!", msg)

def parse_config() -> Namespace:
    parser = ArgParser(config_file_parser_class = YAMLConfigFileParser, default_config_files = ["config.yml"])
    parser.add('-c', '--config', is_config_file=True, help='notification server config in yaml format (.yml)')
    parser.add('-p', '--port', type=int, required=True, help='notification server port')
    return parser.parse_args()

if __name__ == "__main__":
    exit(main())

