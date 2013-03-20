/**
 * (c) 2003-2012 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master
 * Subscription Agreement (or other Terms of Service) separately entered
 * into between you and MuleSoft. If such an agreement is not in
 * place, you may not use the software.
 */

package net.schmizz.sshj.common;

/** SSH message identifiers */
public enum Message {

    UNKNOWN(0),
    DISCONNECT(1),
    IGNORE(2),
    UNIMPLEMENTED(3),
    DEBUG(4),
    SERVICE_REQUEST(5),
    SERVICE_ACCEPT(6),
    KEXINIT(20),
    NEWKEYS(21),

    KEXDH_INIT(30),

    /** { KEXDH_REPLY, KEXDH_GEX_GROUP } */
    KEXDH_31(31),

    KEX_DH_GEX_INIT(32),
    KEX_DH_GEX_REPLY(33),
    KEX_DH_GEX_REQUEST(34),

    USERAUTH_REQUEST(50),
    USERAUTH_FAILURE(51),
    USERAUTH_SUCCESS(52),
    USERAUTH_BANNER(53),

    /** { USERAUTH_PASSWD_CHANGREQ, USERAUTH_PK_OK, USERAUTH_INFO_REQUEST } */
    USERAUTH_60(60),
    USERAUTH_INFO_RESPONSE(61),

    GLOBAL_REQUEST(80),
    REQUEST_SUCCESS(81),
    REQUEST_FAILURE(82),

    CHANNEL_OPEN(90),
    CHANNEL_OPEN_CONFIRMATION(91),
    CHANNEL_OPEN_FAILURE(92),
    CHANNEL_WINDOW_ADJUST(93),
    CHANNEL_DATA(94),
    CHANNEL_EXTENDED_DATA(95),
    CHANNEL_EOF(96),
    CHANNEL_CLOSE(97),
    CHANNEL_REQUEST(98),
    CHANNEL_SUCCESS(99),
    CHANNEL_FAILURE(100);

    private final byte b;

    private static final Message[] cache = new Message[256];

    static {
        for (Message c : Message.values())
            cache[c.toByte()] = c;
        for (int i = 0; i < 256; i++) {
            if (cache[i] == null)
                cache[i] = UNKNOWN;
        }
    }

    public static Message fromByte(byte b) {
        return cache[b];
    }

    private Message(int b) {
        this.b = (byte) b;
    }

    public boolean geq(int num) {
        return b >= num;
    }

    public boolean gt(int num) {
        return b > num;
    }

    public boolean in(int x, int y) {
        return b >= x && b <= y;
    }

    public boolean leq(int num) {
        return b <= num;
    }

    public boolean lt(int num) {
        return b < num;
    }

    public byte toByte() {
        return b;
    }

}
