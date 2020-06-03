from configargparse import ArgParser, Namespace, YAMLConfigFileParser
from sys import exit

from notification_handler import notify
from socket_server import serve

def main() -> int:
    config = parse_config()
    serve(config, handle_msg)
    return 0

def handle_msg(title: str, msg : str) -> None:
    notify(title, msg)

def parse_config() -> Namespace:
    parser = ArgParser(config_file_parser_class = YAMLConfigFileParser, default_config_files = ["config.yml"])
    parser.add('-c', '--config', is_config_file=True, help='notification server config in yaml format (.yml)')
    parser.add('-p', '--port', type=int, required=True, help='notification server port')
    parser.add('-s', '--secrets-file', type=str, required=True, help='secrets file path')
    return parser.parse_args()

if __name__ == "__main__":
    exit(main())

