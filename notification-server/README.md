# Notification Server

This notification server currently opens a TCP socket and accepts connections from any source. It will display each message it receives (UTF-8 seperated by newlines) as desktop notifications using [py-notifier](https://github.com/YuriyLisovskiy/pynotifier).

In the future we will have to specify a custom message format other than newline seperated, this is just for testing. Also this is a pure TCP socket and doesn't include any encryption. We may switch to a TSL socket or similar in the future.

The server is currently tested on Arch Linux, Gnome3 as desktop environment and Python >= 3.6.0.

# Setup

The notification server is a python script whose dependencies are managed by [poetry](https://python-poetry.org/). Poetry will automatically create a virtual environment and handle all project dependencies. To install it, follow the platform specific installation guide found in it's [documentation](https://python-poetry.org/docs/#installation). Depending on your system you may need to add poetry to your PATH.

## Python Version

If you have multiple python versions installed and want to use a specific one (>=3.6.0 should work) you can use the command `poetry env use x.x` in the `notification-server` directory. It will create a virtual environment using the specified python version.

## Installing dependencies

To install the python dependencies run `poetry install` in the `notification-server` directoy. A virtual environment is automatically created by poetry. Poetry will use your current system python version unless specified (see [Python Version](#python-version)) and install all dependencies found in the `pyproject.toml` file.

# Execution

To execute the server you can run the command `poetry run notification-server`.

It will use the configuration saved in `config.yml`. All configuration parameters can also be set as commandline parameters. To find a full list of parameters run `poetry run notification-server --help`.

## Testing the Server

An easy way to test the server is to use the `telnet` client. Connect to `localhost` on port `12345` and send any newline seperated messages. The messages should appear as system notifications which should be visible for 5 seconds.
