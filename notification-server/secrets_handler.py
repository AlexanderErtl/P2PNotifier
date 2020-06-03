from threading import Lock
from pathlib import Path
from secrets import token_bytes
import pickle


class SecretsHandler:
    def __init__(self, secrets_file : str):
        self._secrets_path = Path(secrets_file)
        if not self._secrets_path.exists():
            self.__write_secrets_to_file({})
        self._lock = Lock()

    def get_secrets(self):
        self._lock.acquire()
        secrets = self.__read_secrets_from_file()
        self._lock.release()
        return secrets

    def get_new_secret(self):
        self._lock.acquire()
        secrets = self.__read_secrets_from_file()

        new_identity = self.__generate_new_identity()
        while new_identity in secrets:
            new_identity = self.__generate_new_identity()
        new_secret = self.__generate_new_secret()
        while new_secret in secrets.values():
            new_secret = self.__generate_new_secret()

        secrets[new_identity] = new_secret
        self.__write_secrets_to_file(secrets)
        self._lock.release()
        return new_identity, new_secret

    def __read_secrets_from_file(self):
        return pickle.load(self._secrets_path.open("rb"))

    def __write_secrets_to_file(self, secrets):
        pickle.dump(secrets, self._secrets_path.open("wb"))

    def __generate_new_identity(self):
        return token_bytes(8)

    def __generate_new_secret(self):
        return token_bytes(32)




