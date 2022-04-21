package net.simax_dev.siweb.enums;

/**
 * The CommunicationType decides how the client and server are Communicating with each other
 */
public enum CommunicationType {
    /**
     * The active CommunicationType uses the websocket protocol in order to send updates to the user
     */
    ACTIVE,

    /**
     * The passive CommunicationType lets the user pull new events periodically from the server
     */
    PASSIVE;
}
