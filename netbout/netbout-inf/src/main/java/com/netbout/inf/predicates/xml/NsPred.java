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
package com.netbout.inf.predicates.xml;

import com.netbout.inf.Meta;
import com.netbout.inf.Msg;
import com.netbout.inf.Predicate;
import com.netbout.inf.predicates.AbstractVarargPred;
import com.netbout.spi.Message;
import com.netbout.spi.Urn;
import com.netbout.spi.xml.DomParser;
import com.ymock.util.Logger;
import java.util.List;

/**
 * Namespace predicate.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Meta(name = "ns", extracts = true)
public final class NsPred extends AbstractVarargPred {

    /**
     * Message property.
     */
    public static final String NAMESPACE = "namespace";

    /**
     * Public ctor.
     * @param args The arguments
     */
    public NsPred(final List<Predicate> args) {
        super(args);
    }

    /**
     * Extracts necessary data from message.
     * @param from The message to extract from
     * @param msg Where to extract
     */
    public static void extract(final Message from, final Msg msg) {
        final DomParser parser = new DomParser(from.text());
        if (parser.isXml()) {
            Urn namespace;
            try {
                namespace = parser.namespace();
            } catch (com.netbout.spi.xml.DomValidationException ex) {
                throw new IllegalStateException(ex);
            }
            msg.put(NsPred.NAMESPACE, namespace);
            Logger.debug(
                NsPred.class,
                "#extract(#%d, ..): namespace '%s' found",
                from.number(),
                namespace
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object evaluate(final Msg msg, final int pos) {
        final Urn namespace = Urn.create(
            (String) this.arg(0).evaluate(msg, pos)
        );
        final boolean result = msg.has(this.NAMESPACE, namespace);
        Logger.debug(
            this,
            "#evaluate(#%d, %d): namespace '%s' required: %B",
            msg.number(),
            pos,
            namespace,
            result
        );
        return result;
    }

}
