/**
 * (c) 2003-2012 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master
 * Subscription Agreement (or other Terms of Service) separately entered
 * into between you and MuleSoft. If such an agreement is not in
 * place, you may not use the software.
 */

package net.schmizz.sshj.signature;

import net.schmizz.sshj.common.KeyType;
import net.schmizz.sshj.common.SSHRuntimeException;

import java.security.SignatureException;

/** DSA {@link Signature} */
public class SignatureDSA
        extends AbstractSignature {

    /** A named factory for DSA signature */
    public static class Factory
            implements net.schmizz.sshj.common.Factory.Named<Signature> {

        @Override
        public Signature create() {
            return new SignatureDSA();
        }

        @Override
        public String getName() {
            return KeyType.DSA.toString();
        }

    }

    public SignatureDSA() {
        super("SHA1withDSA");
    }

    @Override
    public byte[] sign() {
        byte[] sig;
        try {
            sig = signature.sign();
        } catch (SignatureException e) {
            throw new SSHRuntimeException(e);
        }

        // sig is in ASN.1
        // SEQUENCE::={ r INTEGER, s INTEGER }

        int rIndex = 3;
        int rLen = sig[rIndex++] & 0xff;
        byte[] r = new byte[rLen];
        System.arraycopy(sig, rIndex, r, 0, r.length);

        int sIndex = rIndex + rLen + 1;
        int sLen = sig[sIndex++] & 0xff;
        byte[] s = new byte[sLen];
        System.arraycopy(sig, sIndex, s, 0, s.length);

        byte[] result = new byte[40];

        // result must be 40 bytes, but length of r and s may not be 20 bytes

        System.arraycopy(r,
                         r.length > 20 ? 1 : 0,
                         result,
                         r.length > 20 ? 0 : 20 - r.length,
                         r.length > 20 ? 20 : r.length);

        System.arraycopy(s,
                         s.length > 20 ? 1 : 0,
                         result,
                         s.length > 20 ? 20 : 40 - s.length,
                         s.length > 20 ? 20 : s.length);

        return result;
    }

    @Override
    public boolean verify(byte[] sig) {
        sig = extractSig(sig);

        // ASN.1
        int frst = (sig[0] & 0x80) != 0 ? 1 : 0;
        int scnd = (sig[20] & 0x80) != 0 ? 1 : 0;

        int length = sig.length + 6 + frst + scnd;
        byte[] tmp = new byte[length];
        tmp[0] = (byte) 0x30;
        tmp[1] = (byte) 0x2c;
        tmp[1] += frst;
        tmp[1] += scnd;
        tmp[2] = (byte) 0x02;
        tmp[3] = (byte) 0x14;
        tmp[3] += frst;
        System.arraycopy(sig, 0, tmp, 4 + frst, 20);
        tmp[4 + tmp[3]] = (byte) 0x02;
        tmp[5 + tmp[3]] = (byte) 0x14;
        tmp[5 + tmp[3]] += scnd;
        System.arraycopy(sig, 20, tmp, 6 + tmp[3] + scnd, 20);
        sig = tmp;

        try {
            return signature.verify(sig);
        } catch (SignatureException e) {
            throw new SSHRuntimeException(e);
        }
    }

}
