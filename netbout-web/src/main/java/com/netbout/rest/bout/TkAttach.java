/**
 * Copyright (c) 2009-2014, netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netbout Inc. located at www.netbout.com.
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
package com.netbout.rest.bout;

import com.jcabi.aspects.Tv;
import com.netbout.rest.RsFailure;
import com.netbout.spi.Attachment;
import com.netbout.spi.Attachments;
import com.netbout.spi.Base;
import com.netbout.spi.Bout;
import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqMultipart;
import org.takes.rq.RqPrint;

/**
 * Attach.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.14
 */
final class TkAttach implements Take {

    static {
        MimeUtil.registerMimeDetector(
            MagicMimeMimeDetector.class.getCanonicalName()
        );
    }

    /**
     * Base.
     */
    private final transient Base base;

    /**
     * Ctor.
     * @param bse Base
     */
    TkAttach(final Base bse) {
        this.base = bse;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final RqMultipart.Smart multi = new RqMultipart.Smart(
            new RqMultipart.Base(req)
        );
        final String name = new RqPrint(multi.single("name")).printBody();
        final File temp = File.createTempFile("netbout", "bin");
        IOUtils.copy(multi.single("file").body(), new FileOutputStream(temp));
        final Bout bout = new RqBout(this.base, req).bout();
        final StringBuilder msg = new StringBuilder(Tv.HUNDRED);
        if (new Attachments.Search(bout.attachments()).exists(name)) {
            msg.append(String.format("attachment '%s' overwritten", name));
        } else {
            try {
                bout.attachments().create(name);
            } catch (final Attachments.InvalidNameException ex) {
                throw new RsFailure(ex);
            }
            msg.append(String.format("attachment '%s' uploaded", name));
        }
        final Collection<?> ctypes = MimeUtil.getMimeTypes(temp);
        final String ctype;
        if (ctypes.isEmpty()) {
            ctype = "application/octet-stream";
        } else {
            ctype = ctypes.iterator().next().toString();
        }
        msg.append(" (").append(temp.length())
            .append(" bytes, ").append(ctype).append(')');
        try {
            bout.attachments().get(name).write(
                new FileInputStream(temp),
                ctype, Long.toString(System.currentTimeMillis())
            );
        } catch (final Attachment.TooBigException
            | Attachment.BrokenContentException ex) {
            throw new RsFailure(ex);
        }
        FileUtils.forceDelete(temp);
        throw new RsForward(new RsFlash(msg.toString()));
    }

}