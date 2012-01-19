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
package com.netbout.shary;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Beginning of document sharing.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlType(name = "beginner", namespace = Slip.NAMESPACE)
@XmlAccessorType(XmlAccessType.NONE)
public final class Slip {

    /**
     * Namespace.
     */
    public static final String NAMESPACE = "urn:woquo:shary:Slip";

    /**
     * Is it an allowing slip or declining?
     */
    private transient boolean allow;

    /**
     * URI of the document to share.
     */
    private transient URI uri;

    /**
     * Author of the document.
     */
    private transient Urn author;

    /**
     * Render it for a reader.
     * @return The text
     */
    public String render() {
        return String.format(
            "%s shared a document with us: \"%s\"",
            this.author,
            this.uri
        );
    }

    /**
     * Get allowing flag.
     * @return Author allowed to see the document?
     */
    @XmlElement(name = "allow", namespace = Slip.NAMESPACE)
    public boolean getAllow() {
        return this.allow;
    }

    /**
     * Get URI.
     * @return The URI of the document
     */
    @XmlElement(name = "uri", namespace = Slip.NAMESPACE)
    public String getURI() {
        return this.uri.toString();
    }

    /**
     * Get author's name.
     * @return The URN of the author
     */
    @XmlElement(name = "author", namespace = Slip.NAMESPACE)
    public String getAuthor() {
        return this.uri.toString();
    }

}
