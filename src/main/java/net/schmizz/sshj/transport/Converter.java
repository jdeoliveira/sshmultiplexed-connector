/**
 * (c) 2003-2012 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master
 * Subscription Agreement (or other Terms of Service) separately entered
 * into between you and MuleSoft. If such an agreement is not in
 * place, you may not use the software.
 */

package net.schmizz.sshj.transport;

import net.schmizz.sshj.transport.cipher.Cipher;
import net.schmizz.sshj.transport.cipher.NoneCipher;
import net.schmizz.sshj.transport.compression.Compression;
import net.schmizz.sshj.transport.mac.MAC;

/**
 * Base class for {@link Encoder} and {@link Decoder}.
 * <p/>
 * From RFC 4253, p. 6
 * <p/>
 * <pre>
 *    Each packet is in the following format:
 *
 *       uint32    packet_length
 *       byte      padding_length
 *       byte[n1]  payload; n1 = packet_length - padding_length - 1
 *       byte[n2]  random padding; n2 = padding_length
 *       byte[m]   mac (Message Authentication Code - MAC); m = mac_length
 * </pre>
 */
abstract class Converter {

    protected Cipher cipher = new NoneCipher();
    protected MAC mac = null;
    protected Compression compression = null;

    protected int cipherSize = 8;
    protected long seq = -1;
    protected boolean authed;

    long getSequenceNumber() {
        return seq;
    }

    void setAlgorithms(Cipher cipher, MAC mac, Compression compression) {
        this.cipher = cipher;
        this.mac = mac;
        this.compression = compression;
        if (compression != null)
            compression.init(getCompressionType());
        this.cipherSize = cipher.getIVSize();
    }

    void setAuthenticated() {
        this.authed = true;
    }

    boolean usingCompression() {
        return compression != null && (authed || !compression.isDelayed());
    }

    abstract Compression.Mode getCompressionType();

}