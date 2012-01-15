<?xml version="1.0"?>
<!--
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
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:nb="http://www.netbout.com"
    version="2.0" exclude-result-prefixes="xs">

    <xsl:include href="/xsl/templates.xsl" />

    <xsl:template match="/">
        <!-- see http://stackoverflow.com/questions/3387127 -->
        <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html&gt;</xsl:text>
        <xsl:apply-templates select="page" />
    </xsl:template>

    <xsl:template match="page">
        <html lang="en-US">
            <head>
                <script type="text/javascript"
                    src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"/>
                <link href="/css/global.css" rel="stylesheet" type="text/css"
                    media="all"></link>
                <link href="/css/layout.css" rel="stylesheet" type="text/css"
                    media="all"></link>
                <link rel="icon" type="image/gif"
                    href="http://img.netbout.com/favicon.ico"/>
                <xsl:call-template name="head" />
            </head>
            <body>
                <xsl:apply-templates select="version" />
                <div id="cap">
                    <div id="incap">
                        <xsl:call-template name="header" />
                    </div>
                </div>
                <section id="content" role="main">
                    <xsl:if test="message != ''">
                        <aside id="error-message">
                            <xsl:value-of select="message"/>
                        </aside>
                    </xsl:if>
                    <xsl:call-template name="content" />
                </section>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="version">
        <aside id="version">
            <xsl:text>r</xsl:text>
            <xsl:value-of select="revision"/>
            <xsl:text> </xsl:text>
            <xsl:call-template name="nano">
                <xsl:with-param name="nano" select="/page/@nano" />
            </xsl:call-template>
        </aside>
    </xsl:template>

    <xsl:template name="header">
        <header id="header">
            <div id="left">
                <a id="logo">
                    <xsl:attribute name="href">
                        <xsl:value-of select="links/link[@rel='home']/@href"/>
                    </xsl:attribute>
                </a>
                <form id="search" method="get" role="search">
                    <xsl:attribute name="action">
                        <xsl:value-of select="/page/links/link[@rel='self']"/>
                    </xsl:attribute>
                    <input name="q" id="search-input" placeholder="Find..."
                        autocomplete="off" size="10" maxlength="120" required="true">
                        <xsl:attribute name="value">
                            <xsl:value-of select="/page/query"/>
                        </xsl:attribute>
                        <xsl:if test="/page/query != ''">
                            <xsl:attribute name="autofocus">
                                <xsl:text>true</xsl:text>
                            </xsl:attribute>
                        </xsl:if>
                    </input>
                </form>
            </div>
            <xsl:if test="identity">
                <nav id="right" role="navigation">
                    <ul>
                        <li>
                            <img id="photo">
                                <xsl:attribute name="src">
                                    <xsl:value-of select="identity/photo"/>
                                </xsl:attribute>
                            </img>
                            <span>
                                <xsl:call-template name="alias">
                                    <xsl:with-param name="alias" select="identity/alias" />
                                </xsl:call-template>
                            </span>
                            <xsl:if test="identity/@helper='true'">
                                <span><xsl:text>&#160;(h)</xsl:text></span>
                            </xsl:if>
                        </li>
                        <li>
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="links/link[@rel='start']/@href"/>
                                </xsl:attribute>
                                <span><xsl:text>Start</xsl:text></span>
                                <span class="red">
                                    <xsl:text>+</xsl:text>
                                </span>
                            </a>
                        </li>
                        <li>
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="links/link[@rel='logout']/@href"/>
                                </xsl:attribute>
                                <span><xsl:text>Logout</xsl:text></span>
                            </a>
                        </li>
                    </ul>
                </nav>
            </xsl:if>
        </header>
    </xsl:template>

</xsl:stylesheet>