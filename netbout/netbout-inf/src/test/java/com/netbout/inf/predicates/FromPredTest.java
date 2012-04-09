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
package com.netbout.inf.predicates;

import com.netbout.inf.Atom;
import com.netbout.inf.Index;
import com.netbout.inf.IndexMocker;
import com.netbout.inf.Predicate;
import com.netbout.inf.atoms.NumberAtom;
import com.netbout.inf.predicates.logic.AndPred;
import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link FromPred}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class FromPredTest {

    /**
     * FromPred can match a message with required position.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void positivelyMatchesMessageAtPosition() throws Exception {
        final Predicate pred = new FromPred(
            Arrays.asList(new Atom[] {new NumberAtom(1L)}),
            new IndexMocker().mock()
        );
        MatcherAssert.assertThat("not matched", !pred.contains(2L));
        MatcherAssert.assertThat("matched", pred.contains(2L));
    }

    /**
     * FromPred can let us select all messages after certain point.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void selectsPortionOfMessages() throws Exception {
        final long total = 10L;
        final long from = 3L;
        final long limit = total - from - 1;
        final Index index = new IndexMocker().mock();
        final Predicate pred = new AndPred(
            Arrays.asList(
                new Atom[] {
                    new FromPred(
                        Arrays.asList(new Atom[] {new NumberAtom(from)}),
                        index
                    ),
                    new LimitPred(
                        Arrays.asList(new Atom[] {new NumberAtom(limit)}),
                        index
                    ),
                }
            ),
            index
        );
        long count = 0L;
        for (int pos = 0; pos < total; pos += 1) {
            if (pred.contains(1L)) {
                count += 1;
            }
        }
        MatcherAssert.assertThat(count, Matchers.equalTo(limit));
    }

}
