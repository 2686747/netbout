/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code accidentally and without intent to use it, please report this
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
package com.netbout.db;

import com.jcabi.manifests.Manifests;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case of {@link Database}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DatabaseTest {

    /**
     * Snapshot of Manifests.
     */
    private transient byte[] snapshot;

    /**
     * Prepare manifests.
     * @throws Exception If there is some problem inside
     */
    @Before
    public void prepare() throws Exception {
        this.snapshot = Manifests.snapshot();
        Manifests.inject("Netbout-JdbcDriver", new DriverMocker("foo").mock());
        Manifests.inject("Netbout-JdbcUrl", "jdbc:foo:");
    }

    /**
     * Prepare manifests.
     * @throws Exception If there is some problem inside
     */
    @After
    public void revert() throws Exception {
        Manifests.revert(this.snapshot);
    }

    /**
     * Database can reconnect if connection is lost.
     * @throws Exception If there is some problem inside
     * @todo #127 This test doesn't reproduce the problem still. I don't know
     *  what to do here exactly. Looks like the problem is bigger than it looks.
     */
    @Test
    @org.junit.Ignore
    public void canReconnectOnAlreadyClosedConnection() throws Exception {
        // @checkstyle MagicNumber (1 line)
        for (int step = 0; step < 100; step += 1) {
            final Connection conn = Database.source().getConnection();
            try {
                final PreparedStatement stmt = conn.prepareStatement(
                    "SELECT name FROM identity"
                );
                try {
                    stmt.execute();
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        }
    }

}