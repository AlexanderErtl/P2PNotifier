# Notification Server

This notification server currently opens a TCP socket and accepts connections from any source. It will display each message it receives (UTF-8 seperated by newlines) as desktop notifications using [py-notifier](https://github.com/YuriyLisovskiy/pynotifier).

The server is currently tested on Arch Linux, Gnome3 as desktop environment and Python 3.8.2.

# Setup

The notification server is a python script whose dependencies are managed by [poetry](https://python-poetry.org/). Poetry will automatically create a virtual environment and handle all project dependencies. To install it, follow the platform specific installation guide found in it's [documentation](https://python-poetry.org/docs/#installation)

After having poetry installed (and added to your execution path) you can run `poetry install` in the `notification-server` directory to install all python dependencies. A virtual environment is automatically created by poetry. Poetry will use your current system python version and install all dependencies found in the `pyproject.toml` file.

# Execution

To execute the server you can run the command `poetry run notification-server`.

It will use the configuration saved in `config.yml`. All configuration parameters can also be set as commandline parameters. To find a full list of parameters run `poetry run notification-server --help`.
