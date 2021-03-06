/**
 * (c) 2003-2012 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master
 * Subscription Agreement (or other Terms of Service) separately entered
 * into between you and MuleSoft. If such an agreement is not in
 * place, you may not use the software.
 */

package net.schmizz.sshj.transport.mac;

/** Message Authentication Code for use in SSH. It usually wraps a javax.crypto.Mac class. */
public interface MAC {

    byte[] doFinal();

    byte[] doFinal(byte[] input);

    void doFinal(byte[] buf, int offset);

    int getBlockSize();

    void init(byte[] key);

    void update(byte[] foo);

    void update(byte[] foo, int start, int len);

    void update(long foo);
}
