//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.23 at 08:16:54 PM KST 
//


package jaxb.test;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;
import org.w3c.dom.Element;


/**
 * <p>Java class for EventListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EventListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="ObjectEvent" type="{axis.epcis.oliot.org}ObjectEventType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="AggregationEvent" type="{axis.epcis.oliot.org}AggregationEventType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="QuantityEvent" type="{axis.epcis.oliot.org}QuantityEventType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="TransactionEvent" type="{axis.epcis.oliot.org}TransactionEventType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="TransformationEvent" type="{axis.epcis.oliot.org}TransformationEventType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="SensorEvent" type="{axis.epcis.oliot.org}SensorEventType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="extension" type="{axis.epcis.oliot.org}EPCISEventListExtensionType"/>
 *         &lt;any processContents='lax' namespace='##other'/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventListType", propOrder = {
    "objectEventOrAggregationEventOrQuantityEvent"
})
public class EventListType {

    @XmlElementRefs({
        @XmlElementRef(name = "ObjectEvent", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "TransactionEvent", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "QuantityEvent", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "TransformationEvent", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "extension", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "SensorEvent", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "AggregationEvent", type = JAXBElement.class, required = false)
    })
    @XmlAnyElement(lax = true)
    protected List<Object> objectEventOrAggregationEventOrQuantityEvent;

    /**
     * Gets the value of the objectEventOrAggregationEventOrQuantityEvent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the objectEventOrAggregationEventOrQuantityEvent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObjectEventOrAggregationEventOrQuantityEvent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Element }
     * {@link JAXBElement }{@code <}{@link ObjectEventType }{@code >}
     * {@link JAXBElement }{@code <}{@link TransactionEventType }{@code >}
     * {@link JAXBElement }{@code <}{@link QuantityEventType }{@code >}
     * {@link JAXBElement }{@code <}{@link TransformationEventType }{@code >}
     * {@link Object }
     * {@link JAXBElement }{@code <}{@link EPCISEventListExtensionType }{@code >}
     * {@link JAXBElement }{@code <}{@link SensorEventType }{@code >}
     * {@link JAXBElement }{@code <}{@link AggregationEventType }{@code >}
     * 
     * 
     */
    public List<Object> getObjectEventOrAggregationEventOrQuantityEvent() {
        if (objectEventOrAggregationEventOrQuantityEvent == null) {
            objectEventOrAggregationEventOrQuantityEvent = new ArrayList<Object>();
        }
        return this.objectEventOrAggregationEventOrQuantityEvent;
    }

}
