/**
 * (c) 2003-2012 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master
 * Subscription Agreement (or other Terms of Service) separately entered
 * into between you and MuleSoft. If such an agreement is not in
 * place, you may not use the software.
 */

package net.schmizz.sshj.common;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;

/** Type of key e.g. rsa, dsa */
public enum KeyType {

    /** SSH identifier for RSA keys */
    RSA("ssh-rsa") {
        @Override
        public PublicKey readPubKeyFromBuffer(String type, Buffer<?> buf)
                throws GeneralSecurityException {
            final BigInteger e, n;
            try {
                e = buf.readMPInt();
                n = buf.readMPInt();
            } catch (Buffer.BufferException be) {
                throw new GeneralSecurityException(be);
            }
            final KeyFactory keyFactory = SecurityUtils.getKeyFactory("RSA");
            return keyFactory.generatePublic(new RSAPublicKeySpec(n, e));
        }

        @Override
        public void putPubKeyIntoBuffer(PublicKey pk, Buffer<?> buf) {
            final RSAPublicKey rsaKey = (RSAPublicKey) pk;
            buf.putString(sType)
                    .putMPInt(rsaKey.getPublicExponent()) // e
                    .putMPInt(rsaKey.getModulus()); // n
        }

        @Override
        protected boolean isMyType(Key key) {
            return (key instanceof RSAPublicKey || key instanceof RSAPrivateKey);
        }

    },

    /** SSH identifier for DSA keys */
    DSA("ssh-dss") {
        @Override
        public PublicKey readPubKeyFromBuffer(String type, Buffer<?> buf)
                throws GeneralSecurityException {
            BigInteger p, q, g, y;
            try {
                p = buf.readMPInt();
                q = buf.readMPInt();
                g = buf.readMPInt();
                y = buf.readMPInt();
            } catch (Buffer.BufferException be) {
                throw new GeneralSecurityException(be);
            }
            final KeyFactory keyFactory = SecurityUtils.getKeyFactory("DSA");
            return keyFactory.generatePublic(new DSAPublicKeySpec(y, p, q, g));
        }

        @Override
        public void putPubKeyIntoBuffer(PublicKey pk, Buffer<?> buf) {
            final DSAPublicKey dsaKey = (DSAPublicKey) pk;
            buf.putString(sType)
                    .putMPInt(dsaKey.getParams().getP()) // p
                    .putMPInt(dsaKey.getParams().getQ()) // q
                    .putMPInt(dsaKey.getParams().getG()) // g
                    .putMPInt(dsaKey.getY()); // y
        }

        @Override
        protected boolean isMyType(Key key) {
            return (key instanceof DSAPublicKey || key instanceof DSAPrivateKey);
        }

    },

    /** Unrecognized */
    UNKNOWN("unknown") {
        @Override
        public PublicKey readPubKeyFromBuffer(String type, Buffer<?> buf)
                throws GeneralSecurityException {
            throw new UnsupportedOperationException("Don't know how to decode key:" + type);
        }

        @Override
        public void putPubKeyIntoBuffer(PublicKey pk, Buffer<?> buf) {
            throw new UnsupportedOperationException("Don't know how to encode key: " + pk);
        }

        @Override
        protected boolean isMyType(Key key) {
            return false;
        }

    };


    protected final String sType;

    private KeyType(String type) {
        this.sType = type;
    }

    public abstract PublicKey readPubKeyFromBuffer(String type, Buffer<?> buf)
            throws GeneralSecurityException;

    public abstract void putPubKeyIntoBuffer(PublicKey pk, Buffer<?> buf);

    protected abstract boolean isMyType(Key key);

    public static KeyType fromKey(Key key) {
        for (KeyType kt : values())
            if (kt.isMyType((key)))
                return kt;
        return UNKNOWN;
    }

    public static KeyType fromString(String sType) {
        for (KeyType kt : values())
            if (kt.sType.equals(sType))
                return kt;
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return sType;
    }

}