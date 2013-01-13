<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:variable name="labels" select="document('result-labels.xml')/SearchResultLabels/Label"/>
    
    <xsl:key name="kStudyByID" match="//CustomList[@type='StudyUnit']" use="Custom[@option='id']" />

    <xsl:template name="result-core-content">
        <xsl:param name="lang"/>
        <xsl:for-each select="LightXmlObject">
            <div class="result">
                <xsl:variable name="studyId" select="CustomList[@type='StudyUnit']/Custom[@option='id']"/>
                
                <xsl:call-template name="element-info">
                    <xsl:with-param name="studyId" select="$studyId"/>
                </xsl:call-template>
                
                <xsl:call-template name="references">
                    <xsl:with-param name="studyId" select="$studyId"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>

                <xsl:variable name="title" select="CustomList[@type='StudyUnit']/Custom[@option='label']"/>
                <div class="study" style="float:left;">
                    <xsl:variable name="url2"
                        select="concat('landingpage.xquery?studyid=', $studyId)"/>
                    <a class="study" href="{$url2}">
                        <xsl:value-of select="$title"/>
                    </a>
                </div>
                <xsl:call-template name="order-box">
                    <xsl:with-param name="studyId" select="$studyId"/>
                    <xsl:with-param name="title" select="$title"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>
            </div>
        </xsl:for-each>

    </xsl:template>
    
    <xsl:template name="result-core-content-grouped">
        <xsl:param name="lang"/>
        <xsl:for-each select="//CustomList[generate-id(.) = generate-id(key('kStudyByID',Custom[@option='id'])[1])]">
            <div class="result">
                <xsl:variable name="studyId" select="Custom[@option='id']" />
                <xsl:variable name="title" select="Custom[@option='label' and @value=$lang]" />
                <div class="studyLarge" style="float:left;">
                    <xsl:variable name="url2"
                        select="concat('landingpage.xquery?studyid=', $studyId)"/>
                    <a class="studyLarge" href="{$url2}">
                        <xsl:value-of select="$title"/>
                    </a>
                </div>
                <xsl:call-template name="order-box">
                    <xsl:with-param name="studyId" select="$studyId"/>
                    <xsl:with-param name="title" select="$title"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>
                <div style="clear: both;"></div>
                <br/>
                <xsl:for-each select="//CustomList[@type='StudyUnit']/Custom[@option='id' and text()=$studyId]/../..">
                    <xsl:call-template name="element-info">
                        <xsl:with-param name="studyId" select="$studyId"/>
                    </xsl:call-template>
                    <xsl:call-template name="references">
                        <xsl:with-param name="studyId" select="$studyId"/>
                        <xsl:with-param name="lang" select="$lang"/>
                    </xsl:call-template>
                </xsl:for-each>
            </div>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="element-info">
        <xsl:param name="studyId"/>
        <p class="contextlink">
            <strong>
                <xsl:variable name="elementType" select="@element"/>
                <xsl:value-of select="$labels[@id=$elementType]/LabelText[@xml:lang=$lang]/Singular/text()"/>: </strong>
            <xsl:if test="@element!='StudyUnit'">
                <xsl:variable name="url"
                    select="concat('codebook.xquery?studyid=', $studyId, '#', @id, '.', @version)"/>
                <a class="contextlink" href="{$url}">
                    <xsl:value-of select="Label"/>
                </a>
            </xsl:if>
            <xsl:if test="@element='StudyUnit'">
                <xsl:variable name="url"
                    select="concat('landingpage.xquery?studyid=', $studyId)"/>
                <a class="contextlink" href="{$url}">
                    <xsl:value-of select="Label"/>
                </a>
            </xsl:if>
            <br/>
            <h3>
                <xsl:value-of select="$labels[@id='html-found-in']/LabelText[@xml:lang=$lang]/text()"/>:
            </h3>
            <br/>
            <em>
                <xsl:apply-templates select="Context"/>
            </em>
            <br/>
        </p>
    </xsl:template>
    
    <xsl:template name="references">
        <xsl:param name="studyId"/>
        <xsl:param name="lang"/>
        <xsl:if test="@element!='StudyUnit'">
            <br/>
            <a href="javascript:;" class="referencedElementsTitle">
                <strong><xsl:value-of select="$labels[@id='html-details']/LabelText[@xml:lang=$lang]/text()"/></strong>
            </a>
            <div class="referencedElementsList">
                <br/>
                <xsl:if test="CustomList[@type='DomainType']">
                    <strong>
                        <xsl:value-of select="$labels[@id='DomainType']/LabelText[@xml:lang=$lang]/text()"/>
                    </strong>
                    <ul type="square">
                        <xsl:for-each select="CustomList[@type='DomainType']">
                            <!--<xsl:variable name="url"
                                select="concat('codebook.xquery?studyid=', $studyId, '#', Custom[@option='id'], '.', Custom[@option='version'])"/>-->
                            <li>
                                <!--<a class="contextlink" href="{$url}">-->
                                <xsl:variable name="domainType" select="Custom/text()"/>
                                <xsl:value-of select="$labels[@id=$domainType]/LabelText[@xml:lang=$lang]/text()"/>
                                <!--</a>-->
                                <xsl:if test="Custom[@option='type'] = 'NumericDomain'">
                                    - <em><xsl:value-of select="Custom/@value"/></em>
                                </xsl:if>
                            </li>
                        </xsl:for-each>
                    </ul>
                </xsl:if>
                
                <xsl:if test="CustomList[@type='RepresentationType']">
                    <strong>
                        <xsl:value-of select="$labels[@id='RepresentationType']/LabelText[@xml:lang=$lang]/text()"/>
                    </strong>
                    <ul type="square">
                        <xsl:for-each select="CustomList[@type='RepresentationType']">
                            <!--<xsl:variable name="url"
                                select="concat('codebook.xquery?studyid=', $studyId, '#', Custom[@option='id'], '.', Custom[@option='version'])"/>-->
                            <li>
                                <!--<a class="contextlink" href="{$url}">-->
                                <xsl:variable name="representationType" select="Custom/text()"/>
                                <xsl:value-of select="$labels[@id=$representationType]/LabelText[@xml:lang=$lang]/text()"/>
                                <!--</a>-->
                                <xsl:if test="Custom[@option='type'] = 'NumericRepresentation'">
                                    - <em><xsl:value-of select="Custom/@value"/></em>
                                </xsl:if>
                            </li>
                        </xsl:for-each>
                    </ul>
                </xsl:if>
                
                <xsl:call-template name="referencedElements">
                    <xsl:with-param name="referencedType" select="'QuestionItem'"/>
                    <xsl:with-param name="studyId" select="$studyId"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>
                <xsl:call-template name="referencedElements">
                    <xsl:with-param name="referencedType" select="'MultipleQuestionItem'"/>
                    <xsl:with-param name="studyId" select="$studyId"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>
                <xsl:call-template name="referencedElements">
                    <xsl:with-param name="referencedType" select="'Variable'"/>
                    <xsl:with-param name="studyId" select="$studyId"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>
                <xsl:call-template name="referencedElements">
                    <xsl:with-param name="referencedType" select="'Category'"/>
                    <xsl:with-param name="studyId" select="$studyId"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>
                <xsl:call-template name="referencedElements">
                    <xsl:with-param name="referencedType" select="'Concept'"/>
                    <xsl:with-param name="studyId" select="$studyId"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>
                <xsl:call-template name="referencedElements">
                    <xsl:with-param name="referencedType" select="'Universe'"/>
                    <xsl:with-param name="studyId" select="$studyId"/>
                    <xsl:with-param name="lang" select="$lang"/>
                </xsl:call-template>
            </div>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="order-box">
        <xsl:param name="studyId"/>
        <xsl:param name="title"/>
        <xsl:param name="lang"/>
        <div style="float:right;">
            <xsl:value-of select="$labels[@id='html-order-study']/LabelText[@xml:lang=$lang]/text()"/>
            <input type="checkbox" name="studyChosen[]" onchange="toggleSubmitButton()" />
            <input type="hidden" name="studyId[]" value="{$studyId}" />
            <input type="hidden" name="studyTitle[]" value="{$title}" />
        </div>
        <div style="clear:both;"/>
    </xsl:template>
    
    <xsl:template match="Context">
        <xsl:for-each select="Hit">
            <p>
                <strong>
                    <xsl:variable name="elementType" select="@elementType"/>
                    <xsl:value-of select="$labels[@id=$elementType]/LabelText[@xml:lang=$lang]/Singular/text()"/>:
                </strong>
                <xsl:copy-of select="*"/>
            </p>
        </xsl:for-each>
        <!--<xsl:copy-of select="*"/>-->
    </xsl:template>

    <xsl:template name="referencedElements">
        <xsl:param name="referencedType"/>
        <xsl:param name="studyId"/>
        <xsl:param name="lang"/>
        <xsl:variable name="referencedElements" select="CustomList[@type=$referencedType]"/>
        <xsl:if test="count($referencedElements) &gt; 0">
            <strong>
                <xsl:value-of select="$labels[@id=$referencedType]/LabelText[@xml:lang=$lang]/Plural/text()"/>
            </strong>
            <ul type="square">
                <xsl:for-each select="$referencedElements">
                    <xsl:variable name="url"
                        select="concat('codebook.xquery?studyid=', $studyId, '#', Custom[@option='id'], '.', Custom[@option='version'])"/>
                    <li>
                        <a class="contextlink" href="{$url}">
                            <xsl:value-of select="Custom[@option='label']"/>
                        </a>
                    </li>
                </xsl:for-each>
            </ul>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>