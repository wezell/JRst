<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  
	xmlns="http://www.w3.org/TR/xhtml1/strict">

  <!-- xdoc is mostly an xhtml document wrapped inside a DOCUMENT tag -->
  <xsl:import href="rst2xhtml.xsl"/>

  <xsl:template match="/document">
    <document>
        <properties>
            <title><xsl:value-of select="title"/></title>
        </properties>
        <body>
          <section>
            <xsl:attribute name="name"><xsl:value-of select="title"/></xsl:attribute>
            <xsl:apply-templates />
          </section>
        </body>
    </document>
  </xsl:template>

  <xsl:template match="title">
    <xsl:choose>
      <xsl:when test="count(ancestor::section) = 0">
      </xsl:when>
      <xsl:otherwise>
        <xsl:element name="h{count(ancestor::section) + 1}">
          <xsl:apply-templates/>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- #286 : Régression sur la génération des méta-données
  TODO peut etre que les xsl de xhtml et xdoc devrait être indépendante -->
  <xsl:template match="docinfo">
    <table class="bodyTable" border="0">
      <col class="docinfo-name" />
      <col class="docinfo-content" />
      <tbody valign="top">
        <xsl:apply-templates/>
      </tbody>
    </table>
  </xsl:template>

</xsl:transform>

