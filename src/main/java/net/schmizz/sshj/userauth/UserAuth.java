/**
 * (c) 2003-2012 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master
 * Subscription Agreement (or other Terms of Service) separately entered
 * into between you and MuleSoft. If such an agreement is not in
 * place, you may not use the software.
 */

package net.schmizz.sshj.userauth;

import net.schmizz.sshj.Service;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.method.AuthMethod;

import java.util.Deque;

/** User authentication API. See RFC 4252. */
public interface UserAuth {

    /**
     * Attempt to authenticate {@code username} using each of {@code methods} in order. {@code nextService} is the
     * {@link Service} that will be enabled on successful authentication.
     * <p/>
     * Authentication fails if there are no method available, i.e. if all the method failed or there were method
     * available but could not be attempted because the server did not allow them. In this case, a {@code
     * UserAuthException} is thrown with its cause as the last authentication failure. Other {@code UserAuthException}'s
     * which may have been ignored may be accessed via {@link #getSavedExceptions()}.
     * <p/>
     * Further attempts may also be made by catching {@code UserAuthException} and retrying with this method.
     *
     * @param username    the user to authenticate
     * @param nextService the service to set on successful authentication
     * @param methods     the {@link AuthMethod}'s to try
     *
     * @throws UserAuthException  in case of authentication failure
     * @throws TransportException if there was a transport-layer error
     */
    void authenticate(String username, Service nextService, Iterable<AuthMethod> methods)
            throws UserAuthException, TransportException;

    /**
     * Returns the authentication banner (if any). In some cases this is available even before the first authentication
     * request has been made.
     *
     * @return the banner, or an empty string if none was received
     */
    String getBanner();

    /** @return saved exceptions that might have been ignored because there were more authentication method available. */
    Deque<UserAuthException> getSavedExceptions();

    /** @return the {@code timeout} for a method to successfully authenticate before it is abandoned. */
    int getTimeout();

    /**
     * @return whether authentication was partially successful. Some server's may be configured to require multiple
     *         authentications; and this value will be {@code true} if at least one of the method supplied succeeded.
     */
    boolean hadPartialSuccess();

    /**
     * Set the {@code timeout} for any method to successfully authenticate before it is abandoned.
     *
     * @param timeout the timeout in seconds
     */
    void setTimeout(int timeout);

}
