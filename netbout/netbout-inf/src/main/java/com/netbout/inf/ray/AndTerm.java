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
package com.netbout.inf.ray;

import com.netbout.inf.Cursor;
import com.netbout.inf.Lattice;
import com.netbout.inf.Ray;
import com.netbout.inf.Term;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * AND term.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
final class AndTerm implements Term {

    /**
     * Terms (also visible from {@link OrTerm}).
     * @checkstyle VisibilityModifier (3 lines)
     */
    @SuppressWarnings("PMD.AvoidProtectedFieldInFinalClass")
    protected final transient Set<Term> terms = new LinkedHashSet<Term>();

    /**
     * The ray we're working with.
     */
    private final transient Ray ray;

    /**
     * Shifter for lattice.
     */
    private final transient Lattice.Shifter shifter = new Lattice.Shifter() {
        @Override
        public Cursor shift(final Cursor crsr, final long msg) {
            if (msg >= crsr.msg().number()) {
                throw new IllegalArgumentException("shift back is prohibited");
            }
            return crsr.shift(
                new PickerTerm(
                    AndTerm.this.ray,
                    AndTerm.this.imap,
                    msg
                )
            );
        }
    };

    /**
     * Hash code, for performance reasons.
     */
    private final transient int hash;

    /**
     * Index map.
     */
    private final transient IndexMap imap;

    /**
     * Public ctor.
     * @param iray The ray to work with
     * @param map The index map
     * @param args Arguments (terms)
     */
    public AndTerm(final Ray iray, final IndexMap map,
        final Collection<Term> args) {
        this.ray = iray;
        this.imap = map;
        this.terms.addAll(AndTerm.compress(args));
        if (this.terms.isEmpty()) {
            this.terms.add(new AlwaysTerm(this.ray, this.imap));
        }
        if (this.terms.size() > 1) {
            this.terms.remove(new AlwaysTerm(this.ray, this.imap));
        }
        this.hash = this.toString().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object term) {
        return term == this || (term instanceof AndTerm
            && this.hashCode() == term.hashCode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        text.append("(AND");
        for (Term term : this.terms) {
            text.append(' ').append(term);
        }
        text.append(')');
        return text.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Lattice lattice() {
        final Lattice lattice = this.ray.lattice();
        lattice.always();
        lattice.and(this.terms);
        return lattice;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor shift(final Cursor cursor) {
        final Lattice lattice = this.lattice();
        Cursor slider = lattice.correct(cursor, this.shifter);
        if (!slider.end()) {
            final ConcurrentMap<Term, Cursor> cache =
                new ConcurrentHashMap<Term, Cursor>();
            slider = this.move(
                this.terms.iterator().next(),
                slider,
                cache
            );
            if (!slider.end()) {
                slider = this.slide(slider, cache, lattice);
            }
        }
        return slider;
    }

    /**
     * Move term one step next.
     * @param term The term to use for movement
     * @param from Where to start
     * @param cache Cached positions of every term
     * @return Matched position (or END)
     */
    private Cursor move(final Term term, final Cursor from,
        final ConcurrentMap<Term, Cursor> cache) {
        if (!cache.containsKey(term)
            || cache.get(term).compareTo(from) >= 0
            || term.getClass().getAnnotation(Term.Volatile.class) != null) {
            cache.put(term, from.shift(term));
        }
        return cache.get(term);
    }

    /**
     * Slide them all down to the first match.
     * @param cursor First expected point to reach
     * @param cache Cached positions of every term
     * @param lattice The lattice to use for corrections
     * @return Matched position (or END)
     */
    private Cursor slide(final Cursor cursor,
        final ConcurrentMap<Term, Cursor> cache, final Lattice lattice) {
        Cursor slider = cursor;
        boolean match;
        do {
            match = true;
            final Cursor expected = slider;
            for (Term term : this.terms) {
                final Cursor above =
                    this.above(lattice.correct(slider, this.shifter));
                slider = this.move(term, above, cache);
                if (!expected.equals(slider)) {
                    match = false;
                    break;
                }
                if (slider.compareTo(above) == 0) {
                    throw new IllegalStateException(
                        String.format("term %s didn't shift %s", term, above)
                    );
                }
                if (slider.compareTo(above) > 0) {
                    throw new IllegalStateException(
                        String.format(
                            "term %s shifted %s up to %s",
                            term,
                            above,
                            cursor
                        )
                    );
                }
            }
        } while (!match && !slider.end());
        return slider;
    }

    /**
     * One step above.
     * @param cursor The cursor
     * @return Cursor with one step higher position
     */
    private Cursor above(final Cursor cursor) {
        if (cursor.msg().number() == Long.MAX_VALUE) {
            throw new IllegalArgumentException("can't use above()");
        }
        return new MemCursor(cursor.msg().number() + 1, this.imap);
    }

    /**
     * Compress terms as much as possible.
     * @param args Arguments (terms)
     * @return Collection of compressed terms
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static Collection<Term> compress(final Collection<Term> args) {
        final Set<Term> output = new LinkedHashSet<Term>();
        for (Term arg : args) {
            if (arg instanceof AndTerm) {
                output.addAll(AndTerm.class.cast(arg).terms);
            } else if (arg instanceof OrTerm
                && OrTerm.class.cast(arg).terms.size() == 1) {
                output.addAll(OrTerm.class.cast(arg).terms);
            } else {
                output.add(arg);
            }
        }
        return output;
    }

}
