<?xml version="1.0" encoding="utf-8"?>

<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
     xmlns:srw_dc="info:srw/schema/1/dc-v1.1" xmlns:nex="http://www.nexml.org/1.0">
	<xsl:template match="nex:nexml">
		<table>
		<xsl:apply-templates/>
		</table>
	</xsl:template>
	
	<xsl:template match="nex:meta">
		<tr>
			<td><xsl:value-of select="@property"/></td>
			<td><xsl:value-of select="@content"/></td>
		</tr>
	</xsl:template>
</xsl:stylesheet>