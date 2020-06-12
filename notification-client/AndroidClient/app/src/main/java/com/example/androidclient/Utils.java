package com.example.androidclient;

public class Utils {
    private static final Utils ourInstance = new Utils();

    public static final int INIT_FAILED = -1;
    public static final String INVALID_INPUT = "Invalid input.";
    public static final int MAX_PORT_VAL = 65535;
    public static final int LAUNCH_SECOND_ACTIVITY = 1;

    public static final String INTENT_IP = "ip";
    public static final String INTENT_PORT = "port";
    public static final String CONNECTION_FAILED = "Connection failed.";
    public static final String INTENT_MESSAGE = "message";
    public static final String INTENT_ACTION_SEND_MESSAGE = "message_send";
    public static final String INTENT_ACTION_MESSAGE_RECEIVED = "message_received";
    public static final String INTENT_ACTION_SOCKET_STATE = "socket_state";

    public static final int MESSAGE_TYPE_RECEIVED = 0;
    public static final int MESSAGE_TYPE_SENT = 1;

    public static final String SOCKET_DISCONNECTED = "socket_disconnected";
    public static final String SOCKET_CONNECTED = "socket_connected";

    public static final String JSON_IDENTITY_KEY = "identity";
    public static final String JSON_SECRET_KEY = "secret";
    public static final String JSON_IP_KEY = "ip";
    public static final String JSON_PORT_KEY = "port";

    public static Utils getInstance() {
        return ourInstance;
    }

    private Utils() {
    }
}
