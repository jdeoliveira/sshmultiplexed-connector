/**
 * (c) 2003-2012 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master
 * Subscription Agreement (or other Terms of Service) separately entered
 * into between you and MuleSoft. If such an agreement is not in
 * place, you may not use the software.
 */

package net.schmizz.sshj.connection.channel;

import net.schmizz.sshj.common.Buffer;
import net.schmizz.sshj.common.ErrorNotifiable;
import net.schmizz.sshj.common.Message;
import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.common.SSHPacket;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.transport.Transport;
import net.schmizz.sshj.transport.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

/**
 * {@link InputStream} for channels. Can {@link #receive(byte[], int, int) receive} data into its buffer for serving to
 * readers.
 */
public final class ChannelInputStream
        extends InputStream
        implements ErrorNotifiable {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Channel chan;
    private final Transport trans;
    private final Window.Local win;
    private final Buffer.PlainBuffer buf;
    private final byte[] b = new byte[1];

    private boolean eof;
    private SSHException error;

    public ChannelInputStream(Channel chan, Transport trans, Window.Local win) {
        this.chan = chan;
        this.trans = trans;
        this.win = win;
        buf = new Buffer.PlainBuffer(chan.getLocalMaxPacketSize());
    }

    @Override
    public int available() {
        synchronized (buf) {
            return buf.available();
        }
    }

    @Override
    public void close() {
        eof();
    }

    public void eof() {
        synchronized (buf) {
            if (!eof) {
                eof = true;
                buf.notifyAll();
            }
        }
    }

    @Override
    public synchronized void notifyError(SSHException error) {
        this.error = error;
        eof();
    }

    @Override
    public int read()
            throws IOException {
        synchronized (b) {
            return read(b, 0, 1) == -1 ? -1 : b[0] & 0xff;
        }
    }

    @Override
    public int read(byte[] b, int off, int len)
            throws IOException {
        synchronized (buf) {
            for (; ; ) {
                if (buf.available() > 0)
                    break;
                if (eof)
                    if (error != null)
                        throw error;
                    else
                        return -1;
                try {
                    buf.wait();
                } catch (InterruptedException e) {
                    throw (IOException) new InterruptedIOException().initCause(e);
                }
            }
            if (len > buf.available())
                len = buf.available();
            buf.readRawBytes(b, off, len);
            if (buf.rpos() > win.getMaxPacketSize() && buf.available() == 0)
                buf.clear();
        }

        if (!chan.getAutoExpand())
            checkWindow();

        return len;
    }

    public void receive(byte[] data, int offset, int len)
            throws ConnectionException, TransportException {
        if (eof)
            throw new ConnectionException("Getting data on EOF'ed stream");
        synchronized (buf) {
            buf.putRawBytes(data, offset, len);
            buf.notifyAll();
        }
        win.consume(len);
        if (chan.getAutoExpand())
            checkWindow();
    }

    private void checkWindow()
            throws TransportException {
        synchronized (win) {
            final long adjustment = win.neededAdjustment();
            if (adjustment > 0) {
                log.debug("Sending SSH_MSG_CHANNEL_WINDOW_ADJUST to #{} for {} bytes", chan.getRecipient(), adjustment);
                trans.write(new SSHPacket(Message.CHANNEL_WINDOW_ADJUST)
                                    .putUInt32(chan.getRecipient()).putUInt32(adjustment));
                win.expand(adjustment);
            }
        }
    }

    @Override
    public String toString() {
        return "< ChannelInputStream for Channel #" + chan.getID() + " >";
    }

}