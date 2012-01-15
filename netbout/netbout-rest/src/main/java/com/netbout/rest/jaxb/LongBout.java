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
package com.netbout.rest.jaxb;

import com.netbout.hub.Hub;
import com.netbout.rest.StageCoordinates;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import com.netbout.spi.Urn;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bout convertable to XML through JAXB.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlRootElement(name = "bout")
@XmlAccessorType(XmlAccessType.NONE)
public final class LongBout {

    /**
     * The bus.
     */
    private final transient Hub hub;

    /**
     * The bout.
     */
    private final transient Bout bout;

    /**
     * Stage coordinates.
     */
    private final transient StageCoordinates coords;

    /**
     * Search keyword.
     */
    private final transient String query;

    /**
     * The URI builder.
     */
    private final transient UriBuilder builder;

    /**
     * The viewer of it.
     */
    private final transient Identity viewer;

    /**
     * Public ctor for JAXB.
     */
    public LongBout() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Private ctor.
     * @param ihub The hub
     * @param bot The bout
     * @param crds The coordinates of the stage to render
     * @param keyword Search keyword
     * @param bldr The builder of URIs
     * @param vwr The viewer
     * @checkstyle ParameterNumber (3 lines)
     */
    public LongBout(final Hub ihub, final Bout bot, final StageCoordinates crds,
        final String keyword, final UriBuilder bldr, final Identity vwr) {
        this.hub = ihub;
        this.bout = bot;
        this.coords = crds;
        this.query = keyword;
        this.builder = bldr;
        this.viewer = vwr;
    }

    /**
     * Get number.
     * @return The number
     */
    @XmlElement
    public Long getNumber() {
        return this.bout.number();
    }

    /**
     * Get its title.
     * @return The title
     */
    @XmlElement
    public String getTitle() {
        return this.bout.title();
    }

    /**
     * List of stages.
     * @return The list
     */
    @XmlElement(name = "stage")
    @XmlElementWrapper(name = "stages")
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public List<ShortStage> getStages() {
        final List<ShortStage> stages = new ArrayList<ShortStage>();
        for (Urn identity : this.coords.all()) {
            stages.add(new ShortStage(identity, this.builder.clone()));
        }
        return stages;
    }

    /**
     * Get XML of one stage.
     * @return The XML
     */
    @XmlElement
    public LongStage getStage() {
        LongStage stage = null;
        if (!this.coords.stage().isEmpty()) {
            stage = new LongStage(this.hub, this.bout, this.coords);
        }
        return stage;
    }

    /**
     * List of messages in it.
     * @return The list
     */
    @XmlElement(name = "message")
    @XmlElementWrapper(name = "messages")
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public List<LongMessage> getMessages() {
        final List<LongMessage> messages = new ArrayList<LongMessage>();
        for (Message msg : this.bout.messages(this.query)) {
            messages.add(new LongMessage(msg));
        }
        return messages;
    }

    /**
     * List of participants.
     * @return The list
     */
    @XmlElement(name = "participant")
    @XmlElementWrapper(name = "participants")
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Collection<LongParticipant> getParticipants() {
        final Collection<LongParticipant> dudes =
            new ArrayList<LongParticipant>();
        for (Participant dude : this.bout.participants()) {
            dudes.add(new LongParticipant(dude, this.builder, this.viewer));
        }
        return dudes;
    }

}