//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.17 at 06:25:15 PM CET 
//


package org.openhab.ui.cometvisu.internal.config.beans;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="meta" type="{}meta" minOccurs="0"/&gt;
 *         &lt;element name="page" type="{}page"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="design" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute ref="{}backend"/&gt;
 *       &lt;attribute ref="{}bind_click_to_widget"/&gt;
 *       &lt;attribute name="scroll_speed" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="max_mobile_screen_width" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="min_column_width" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="default_columns" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="lib_version" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute name="screensave_time" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="screensave_page" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "meta",
    "page"
})
@XmlRootElement(name = "pages")
public class Pages {

    protected Meta meta;
    @XmlElement(required = true)
    protected Page page;
    @XmlAttribute(name = "design", required = true)
    protected String design;
    @XmlAttribute(name = "backend")
    protected String backend;
    @XmlAttribute(name = "bind_click_to_widget")
    protected Boolean bindClickToWidget;
    @XmlAttribute(name = "scroll_speed")
    protected BigDecimal scrollSpeed;
    @XmlAttribute(name = "max_mobile_screen_width")
    protected BigDecimal maxMobileScreenWidth;
    @XmlAttribute(name = "min_column_width")
    protected BigDecimal minColumnWidth;
    @XmlAttribute(name = "default_columns")
    protected BigDecimal defaultColumns;
    @XmlAttribute(name = "lib_version", required = true)
    protected BigInteger libVersion;
    @XmlAttribute(name = "screensave_time")
    protected BigDecimal screensaveTime;
    @XmlAttribute(name = "screensave_page")
    protected String screensavePage;

    /**
     * Gets the value of the meta property.
     * 
     * @return
     *     possible object is
     *     {@link Meta }
     *     
     */
    public Meta getMeta() {
        return meta;
    }

    /**
     * Sets the value of the meta property.
     * 
     * @param value
     *     allowed object is
     *     {@link Meta }
     *     
     */
    public void setMeta(Meta value) {
        this.meta = value;
    }

    /**
     * Gets the value of the page property.
     * 
     * @return
     *     possible object is
     *     {@link Page }
     *     
     */
    public Page getPage() {
        return page;
    }

    /**
     * Sets the value of the page property.
     * 
     * @param value
     *     allowed object is
     *     {@link Page }
     *     
     */
    public void setPage(Page value) {
        this.page = value;
    }

    /**
     * Gets the value of the design property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDesign() {
        return design;
    }

    /**
     * Sets the value of the design property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDesign(String value) {
        this.design = value;
    }

    /**
     * Gets the value of the backend property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBackend() {
        return backend;
    }

    /**
     * Sets the value of the backend property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBackend(String value) {
        this.backend = value;
    }

    /**
     * Gets the value of the bindClickToWidget property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isBindClickToWidget() {
        return bindClickToWidget;
    }

    /**
     * Sets the value of the bindClickToWidget property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBindClickToWidget(Boolean value) {
        this.bindClickToWidget = value;
    }

    /**
     * Gets the value of the scrollSpeed property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getScrollSpeed() {
        return scrollSpeed;
    }

    /**
     * Sets the value of the scrollSpeed property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setScrollSpeed(BigDecimal value) {
        this.scrollSpeed = value;
    }

    /**
     * Gets the value of the maxMobileScreenWidth property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMaxMobileScreenWidth() {
        return maxMobileScreenWidth;
    }

    /**
     * Sets the value of the maxMobileScreenWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMaxMobileScreenWidth(BigDecimal value) {
        this.maxMobileScreenWidth = value;
    }

    /**
     * Gets the value of the minColumnWidth property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMinColumnWidth() {
        return minColumnWidth;
    }

    /**
     * Sets the value of the minColumnWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMinColumnWidth(BigDecimal value) {
        this.minColumnWidth = value;
    }

    /**
     * Gets the value of the defaultColumns property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getDefaultColumns() {
        return defaultColumns;
    }

    /**
     * Sets the value of the defaultColumns property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setDefaultColumns(BigDecimal value) {
        this.defaultColumns = value;
    }

    /**
     * Gets the value of the libVersion property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getLibVersion() {
        return libVersion;
    }

    /**
     * Sets the value of the libVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setLibVersion(BigInteger value) {
        this.libVersion = value;
    }

    /**
     * Gets the value of the screensaveTime property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getScreensaveTime() {
        return screensaveTime;
    }

    /**
     * Sets the value of the screensaveTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setScreensaveTime(BigDecimal value) {
        this.screensaveTime = value;
    }

    /**
     * Gets the value of the screensavePage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScreensavePage() {
        return screensavePage;
    }

    /**
     * Sets the value of the screensavePage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScreensavePage(String value) {
        this.screensavePage = value;
    }

}
