/**
 * (c) 2003-2012 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master
 * Subscription Agreement (or other Terms of Service) separately entered
 * into between you and MuleSoft. If such an agreement is not in
 * place, you may not use the software.
 */

package net.schmizz.sshj.xfer.scp;

import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.xfer.LocalFileFilter;
import net.schmizz.sshj.xfer.LocalSourceFile;
import net.schmizz.sshj.xfer.TransferListener;
import net.schmizz.sshj.xfer.scp.SCPEngine.Arg;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/** Support for uploading files over a connected link using SCP. */
public final class SCPUploadClient {

    private final SCPEngine engine;
    private LocalFileFilter uploadFilter;

    SCPUploadClient(SCPEngine engine) {
        this.engine = engine;
    }

    /** Upload a local file from {@code localFile} to {@code targetPath} on the remote host. */
    public synchronized int copy(LocalSourceFile sourceFile, String remotePath)
            throws IOException {
        engine.cleanSlate();
        try {
            startCopy(sourceFile, remotePath);
        } finally {
            engine.exit();
        }
        return engine.getExitStatus();
    }

    public void setUploadFilter(LocalFileFilter uploadFilter) {
        this.uploadFilter = uploadFilter;
    }

    private synchronized void startCopy(LocalSourceFile sourceFile, String targetPath)
            throws IOException {
        List<Arg> args = new LinkedList<Arg>();
        args.add(Arg.SINK);
        args.add(Arg.RECURSIVE);
        if (sourceFile.providesAtimeMtime())
            args.add(Arg.PRESERVE_TIMES);
        engine.execSCPWith(args, targetPath);
        engine.check("Start status OK");
        process(engine.getTransferListener(), sourceFile);
    }

    private void process(TransferListener listener, LocalSourceFile f)
            throws IOException {
        if (f.isDirectory()) {
            sendDirectory(listener.directory(f.getName()), f);
        } else if (f.isFile()) {
            sendFile(listener.file(f.getName(), f.getLength()), f);
        } else
            throw new IOException(f + " is not a regular file or directory");
    }

    private void sendDirectory(TransferListener listener, LocalSourceFile f)
            throws IOException {
        preserveTimeIfPossible(f);
        engine.sendMessage("D0" + getPermString(f) + " 0 " + f.getName());
        for (LocalSourceFile child : f.getChildren(uploadFilter))
            process(listener, child);
        engine.sendMessage("E");
    }

    private void sendFile(StreamCopier.Listener listener, LocalSourceFile f)
            throws IOException {
        preserveTimeIfPossible(f);
        final InputStream src = f.getInputStream();
        try {
            engine.sendMessage("C0" + getPermString(f) + " " + f.getLength() + " " + f.getName());
            engine.transferToRemote(listener, src, f.getLength());
            engine.signal("Transfer done");
            engine.check("Remote agrees transfer done");
        } finally {
            IOUtils.closeQuietly(src);
        }
    }

    private void preserveTimeIfPossible(LocalSourceFile f)
            throws IOException {
        if (f.providesAtimeMtime())
            engine.sendMessage("T" + f.getLastModifiedTime() + " 0 " + f.getLastAccessTime() + " 0");
    }

    private String getPermString(LocalSourceFile f)
            throws IOException {
        return Integer.toOctalString(f.getPermissions() & 07777);
    }

}
