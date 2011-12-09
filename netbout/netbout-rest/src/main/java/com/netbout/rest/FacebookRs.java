/**
 * Copyright (c) 2009-2011, netBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code occasionally and without intent to use it, please report this
 * incident to the author by email.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.netbout.rest;

import com.netbout.hub.User;
import com.netbout.rest.page.PageBuilder;
import com.netbout.spi.Identity;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.rexsl.core.Manifests;
import com.ymock.util.Logger;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.io.IOUtils;

/**
 * Facebook authentication page.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/fb")
public final class FacebookRs extends AbstractRs {

    /**
     * Facebook authentication page (callback hits it).
     * @param code Facebook "authorization code"
     * @return The JAX-RS response
     */
    @GET
    public Response fbauth(@QueryParam("code") final String code) {
        this.logoff();
        Identity identity;
        try {
            identity = this.authenticate(code);
        } catch (IOException ex) {
            throw new ForwardException(this, this.base().path("/g"), ex);
        }
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this)
            .authenticated(identity)
            .status(Response.Status.SEE_OTHER)
            .location(this.base().build())
            .build();
    }

    /**
     * Authenticate the user through facebook.
     * @param code Facebook "authorization code"
     * @return The identity found
     * @throws IOException If some problem with FB
     */
    private Identity authenticate(final String code) throws IOException {
        final String token = this.token(code);
        final com.restfb.types.User fbuser = this.fbUser(token);
        assert fbuser != null;
        final User user = this.hub().user(fbuser.getId());
        Identity identity;
        try {
            identity = user.identity(fbuser.getId());
        } catch (com.netbout.spi.UnreachableIdentityException ex) {
            throw new IllegalStateException(
                String.format(
                    "Facebook identity '%s' is not reachable: %s",
                    fbuser.getId(),
                    ex
                )
            );
        }
        identity.alias(fbuser.getName());
        identity.setPhoto(
            UriBuilder
                .fromPath("https://graph.facebook.com/{id}/picture")
                .build(fbuser.getId())
                .toURL()
        );
        return identity;
    }

    /**
     * Retrieve facebook access token.
     * @param code Facebook "authorization code"
     * @return The token
     * @throws IOException If some problem with FB
     */
    private String token(final String code) throws IOException {
        final String response = this.retrieve(
            UriBuilder
                // @checkstyle MultipleStringLiterals (5 lines)
                .fromPath("https://graph.facebook.com/oauth/access_token")
                .queryParam("client_id", Manifests.read("Netbout-FbId"))
                .queryParam(
                    "redirect_uri",
                    this.base().path("/fb").build()
                )
                .queryParam("client_secret", Manifests.read("Netbout-FbSecret"))
                .queryParam("code", code)
                .build()
        );
        final String[] sectors = response.split("&");
        for (String sector : sectors) {
            final String[] pair = sector.split("=");
            if (pair.length != 2) {
                throw new IOException(
                    String.format(
                        "Invalid response: '%s'",
                        response
                    )
                );
            }
            if ("access_token".equals(pair[0])) {
                return pair[1];
            }
        }
        throw new IOException(
            String.format(
                "Access token not found in response: '%s'",
                response
            )
        );
    }

    /**
     * Get user name from Facebook, but the code provided.
     * @param token Facebook access token
     * @return The user found in FB
     * @throws IOException If some problem with FB
     */
    private com.restfb.types.User fbUser(final String token)
        throws IOException {
        try {
            final FacebookClient client = new DefaultFacebookClient(token);
            return client.fetchObject("me", com.restfb.types.User.class);
        } catch (com.restfb.exception.FacebookException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Retrive data from the URL through HTTP request.
     * @param uri The URI
     * @return The response, text body
     * @throws IOException If some problem with FB
     */
    private String retrieve(final URI uri) throws IOException {
        final long start = System.currentTimeMillis();
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) uri.toURL().openConnection();
        } catch (java.net.MalformedURLException ex) {
            throw new IOException(ex);
        }
        try {
            return IOUtils.toString(conn.getInputStream());
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException(ex);
        } finally {
            conn.disconnect();
            Logger.debug(
                this,
                "#retrieve(%s): done [%d] in %dms",
                uri,
                conn.getResponseCode(),
                System.currentTimeMillis() - start
            );
        }
    }

}
