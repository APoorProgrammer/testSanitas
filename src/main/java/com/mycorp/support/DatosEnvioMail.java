
package com.mycorp.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for datosEnvioMail complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="datosEnvioMail">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="adjuntos" type="{http://ws.mail.sanitas.es/}ficheroAdjunto" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="asunto" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dias" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="emailA" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="emailCC" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="emailCCO" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="emailDe" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="estado" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="funcionalidad" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="idMensaje" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="idioma" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="params" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="texto" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tipoProgramacion" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="fInsercion" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="fProgramacion" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "datosEnvioMail", propOrder = {
    "adjuntos",
    "asunto",
    "dias",
    "emailA",
    "emailCC",
    "emailCCO",
    "emailDe",
    "estado",
    "funcionalidad",
    "idMensaje",
    "idioma",
    "params",
    "texto",
    "tipoProgramacion",
    "fInsercion",
    "fProgramacion"
})
public class DatosEnvioMail
    implements Serializable
{

    @XmlElement(nillable = true)
    protected List<FicheroAdjunto> adjuntos;
    protected String asunto;
    protected String dias;
    protected String emailA;
    protected String emailCC;
    protected String emailCCO;
    protected String emailDe;
    protected int estado;
    protected int funcionalidad;
    protected long idMensaje;
    protected String idioma;
    @XmlElement(nillable = true)
    protected List<String> params;
    protected String texto;
    protected int tipoProgramacion;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar fInsercion;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar fProgramacion;

    /**
     * Gets the value of the adjuntos property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the adjuntos property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAdjuntos().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FicheroAdjunto }
     *
     *
     */
    public List<FicheroAdjunto> getAdjuntos() {
        if (adjuntos == null) {
            adjuntos = new ArrayList<FicheroAdjunto>();
        }
        return this.adjuntos;
    }

    /**
     * Gets the value of the asunto property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAsunto() {
        return asunto;
    }

    /**
     * Sets the value of the asunto property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAsunto(String value) {
        this.asunto = value;
    }

    /**
     * Gets the value of the dias property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDias() {
        return dias;
    }

    /**
     * Sets the value of the dias property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDias(String value) {
        this.dias = value;
    }

    /**
     * Gets the value of the emailA property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getEmailA() {
        return emailA;
    }

    /**
     * Sets the value of the emailA property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setEmailA(String value) {
        this.emailA = value;
    }

    /**
     * Gets the value of the emailCC property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getEmailCC() {
        return emailCC;
    }

    /**
     * Sets the value of the emailCC property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setEmailCC(String value) {
        this.emailCC = value;
    }

    /**
     * Gets the value of the emailCCO property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getEmailCCO() {
        return emailCCO;
    }

    /**
     * Sets the value of the emailCCO property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setEmailCCO(String value) {
        this.emailCCO = value;
    }

    /**
     * Gets the value of the emailDe property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getEmailDe() {
        return emailDe;
    }

    /**
     * Sets the value of the emailDe property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setEmailDe(String value) {
        this.emailDe = value;
    }

    /**
     * Gets the value of the estado property.
     *
     */
    public int getEstado() {
        return estado;
    }

    /**
     * Sets the value of the estado property.
     *
     */
    public void setEstado(int value) {
        this.estado = value;
    }

    /**
     * Gets the value of the funcionalidad property.
     *
     */
    public int getFuncionalidad() {
        return funcionalidad;
    }

    /**
     * Sets the value of the funcionalidad property.
     *
     */
    public void setFuncionalidad(int value) {
        this.funcionalidad = value;
    }

    /**
     * Gets the value of the idMensaje property.
     *
     */
    public long getIdMensaje() {
        return idMensaje;
    }

    /**
     * Sets the value of the idMensaje property.
     *
     */
    public void setIdMensaje(long value) {
        this.idMensaje = value;
    }

    /**
     * Gets the value of the idioma property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getIdioma() {
        return idioma;
    }

    /**
     * Sets the value of the idioma property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setIdioma(String value) {
        this.idioma = value;
    }

    /**
     * Gets the value of the params property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the params property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParams().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     *
     *
     */
    public List<String> getParams() {
        if (params == null) {
            params = new ArrayList<String>();
        }
        return this.params;
    }

    /**
     * Gets the value of the texto property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTexto() {
        return texto;
    }

    /**
     * Sets the value of the texto property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTexto(String value) {
        this.texto = value;
    }

    /**
     * Gets the value of the tipoProgramacion property.
     *
     */
    public int getTipoProgramacion() {
        return tipoProgramacion;
    }

    /**
     * Sets the value of the tipoProgramacion property.
     *
     */
    public void setTipoProgramacion(int value) {
        this.tipoProgramacion = value;
    }

    /**
     * Gets the value of the fInsercion property.
     *
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getFInsercion() {
        return fInsercion;
    }

    /**
     * Sets the value of the fInsercion property.
     *
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public void setFInsercion(XMLGregorianCalendar value) {
        this.fInsercion = value;
    }

    /**
     * Gets the value of the fProgramacion property.
     *
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getFProgramacion() {
        return fProgramacion;
    }

    /**
     * Sets the value of the fProgramacion property.
     *
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public void setFProgramacion(XMLGregorianCalendar value) {
        this.fProgramacion = value;
    }

}
