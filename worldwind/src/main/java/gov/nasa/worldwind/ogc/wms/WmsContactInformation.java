/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsContactInformation extends XmlModel {

    protected QName contactPosition;

    protected QName contactVoiceTelephone;

    protected QName contactFacsimileTelephone;

    protected QName contactElectronicMailAddress;

    protected QName contactPersonPrimary;

    protected QName contactAddress;

    protected QName contactPerson;

    protected QName contactOrganization;

    public WmsContactInformation(String namespaceUri) {
        super(namespaceUri);

        this.initialize();
    }

    private void initialize() {
        contactPosition = new QName(this.getNamespaceUri(), "ContactPosition");
        contactVoiceTelephone = new QName(this.getNamespaceUri(), "ContactVoiceTelephone");
        contactFacsimileTelephone = new QName(this.getNamespaceUri(), "ContactFacsimileTelephone");
        contactElectronicMailAddress = new QName(this.getNamespaceUri(), "ContactElectronicMailAddress");
        contactPersonPrimary = new QName(this.getNamespaceUri(), "ContactPersonPrimary");
        contactAddress = new QName(this.getNamespaceUri(), "ContactAddress");
        contactPerson = new QName(this.getNamespaceUri(), "ContactPerson");
        contactOrganization = new QName(this.getNamespaceUri(), "ContactOrganization");
    }

//    @Override
//    protected void doParseEventContent(XmlEventParserContext ctx, XmlEvent event, Object... args)
//        throws XMLStreamException
//    {
//        if (ctx.isStartElement(event, contactPosition))
//        {
//            this.setPosition(ctx.getStringParser().parseString(ctx, event));
//        }
//        else if (ctx.isStartElement(event, contactVoiceTelephone))
//        {
//            this.setVoiceTelephone(ctx.getStringParser().parseString(ctx, event));
//        }
//        else if (ctx.isStartElement(event, contactFacsimileTelephone))
//        {
//            this.setFacsimileTelephone(ctx.getStringParser().parseString(ctx, event));
//        }
//        else if (ctx.isStartElement(event, contactElectronicMailAddress))
//        {
//            this.setElectronicMailAddress(ctx.getStringParser().parseString(ctx, event));
//        }
//        else if (ctx.isStartElement(event, contactPersonPrimary))
//        {
//            String[] sa = this.parseContactPersonPrimary(ctx, event);
//            this.setPersonPrimary(sa[0]);
//            this.setOrganization(sa[1]);
//        }
//        else if (ctx.isStartElement(event, CONTACT_ADDRESS))
//        {
//            XmlEventParser parser = this.allocate(ctx, event);
//            if (parser != null)
//            {
//                Object o = parser.parse(ctx, event, args);
//                if (o != null && o instanceof WmsAddress)
//                    this.setContactAddress((WmsAddress) o);
//            }
//        }
//    }
//
//    protected String[] parseContactPersonPrimary(XmlEventParserContext ctx, XmlEvent cppEvent) throws XMLStreamException
//    {
//        String[] items = new String[2];
//
//        for (XmlEvent event = ctx.nextEvent(); event != null; event = ctx.nextEvent())
//        {
//            if (ctx.isEndElement(event, cppEvent))
//                return items;
//
//            if (ctx.isStartElement(event, contactPerson))
//            {
//                items[0] = ctx.getStringParser().parseString(ctx, event);
//            }
//            else if (ctx.isStartElement(event, contactOrganization))
//            {
//                items[1] = ctx.getStringParser().parseString(ctx, event);
//            }
//        }
//
//        return null;
//    }

    public String getPersonPrimary() {
        XmlModel personPrimary = (XmlModel) this.getField(this.contactPersonPrimary);
        return ((XmlModel) personPrimary.getField(this.contactPerson)).getField(XmlModel.CHARACTERS_CONTENT).toString();
    }

    protected void setPersonPrimary(String personPrimary) {
        XmlModel currentPersonPrimary = (XmlModel) this.getField(this.contactPersonPrimary);
        if (currentPersonPrimary == null) {
            currentPersonPrimary = new XmlModel(this.getNamespaceUri());
            this.setField(this.contactPersonPrimary, currentPersonPrimary);
        }
        XmlModel person = new XmlModel(this.getNamespaceUri());
        person.setField(XmlModel.CHARACTERS_CONTENT, personPrimary);
        currentPersonPrimary.setField(this.contactPerson, person);
    }

    public String getOrganization() {
        XmlModel personPrimary = (XmlModel) this.getField(this.contactPersonPrimary);
        return ((XmlModel) personPrimary.getField(this.contactOrganization)).getField(XmlModel.CHARACTERS_CONTENT).toString();
    }

    protected void setOrganization(String organization) {
        XmlModel currentOrganization = (XmlModel) this.getField(this.contactPersonPrimary);
        if (currentOrganization == null) {
            currentOrganization = new XmlModel(this.getNamespaceUri());
            this.setField(this.contactPersonPrimary, currentOrganization);
        }
        XmlModel org = new XmlModel(this.getNamespaceUri());
        org.setField(XmlModel.CHARACTERS_CONTENT, organization);
        currentOrganization.setField(this.contactPerson, org);
    }

    public String getPosition() {
        return this.getChildCharacterValue(this.contactPosition);
    }

    protected void setPosition(String position) {
        this.setChildCharacterValue(this.contactPosition, position);
    }

    public String getVoiceTelephone() {
        return this.getChildCharacterValue(this.contactVoiceTelephone);
    }

    protected void setVoiceTelephone(String voiceTelephone) {
        this.setChildCharacterValue(this.contactVoiceTelephone, voiceTelephone);
    }

    public String getFacsimileTelephone() {
        return this.getChildCharacterValue(this.contactFacsimileTelephone);
    }

    protected void setFacsimileTelephone(String facsimileTelephone) {
        this.setChildCharacterValue(this.contactFacsimileTelephone, facsimileTelephone);
    }

    public String getElectronicMailAddress() {
        return this.getChildCharacterValue(this.contactElectronicMailAddress);
    }

    protected void setElectronicMailAddress(String electronicMailAddress) {
        this.setChildCharacterValue(this.contactElectronicMailAddress, electronicMailAddress);
    }

    public WmsAddress getContactAddress() {
        return (WmsAddress) this.getField(this.contactAddress);
    }

    protected void setContactAddress(WmsAddress contactAddress) {
        this.setField(this.contactAddress, contactAddress);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("PersonPrimary: ").append(this.getPersonPrimary() != null ? this.getPersonPrimary() : "none").append("\n");
        sb.append("Organization: ").append(this.getOrganization() != null ? this.getOrganization() : "none").append("\n");
        sb.append("Position: ").append(this.getPosition() != null ? this.getPosition() : "none").append("\n");
        sb.append("VoiceTelephone: ").append(this.getVoiceTelephone() != null ? this.getVoiceTelephone() : "none").append("\n");
        sb.append("FacsimileTelephone: ").append(
            this.getFacsimileTelephone() != null ? this.getFacsimileTelephone() : "none").append("\n");
        sb.append("ElectronicMailAddress: ").append(
            this.getElectronicMailAddress() != null ? this.getElectronicMailAddress() : "none").append("\n");
        sb.append(this.contactAddress != null ? this.contactAddress : "none");

        return sb.toString();
    }
}
