/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.TextModel;
import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsContactInformation extends XmlModel {

    protected String contactPosition;

    protected String contactVoiceTelephone;

    protected String contactFacsimileTelephone;

    protected String contactElectronicMailAddress;

    protected WmsAddress contactAddress;

    protected String contactPerson;

    protected String contactOrganization;

    public WmsContactInformation(String namespaceUri) {
        super(namespaceUri);
    }

    public String getPersonPrimary() {
        return this.contactPerson;
    }

    public String getOrganization() {
        return this.contactOrganization;
    }

    public String getPosition() {
        return this.contactPosition;
    }

    public String getVoiceTelephone() {
        return this.contactVoiceTelephone;
    }

    public String getFacsimileTelephone() {
        return this.contactFacsimileTelephone;
    }

    public String getElectronicMailAddress() {
        return this.contactElectronicMailAddress;
    }

    public WmsAddress getContactAddress() {
        return this.contactAddress;
    }

    @Override
    public void setField(String keyName, Object value) {
        if (keyName.equals("ContactPosition")) {
            this.contactPosition = ((TextModel) value).getValue();
        } else if (keyName.equals("ContactVoiceTelephone")) {
            this.contactVoiceTelephone = ((TextModel) value).getValue();
        } else if (keyName.equals("ContactFacsimileNumber")) {
            this.contactFacsimileTelephone = ((TextModel) value).getValue();
        } else if (keyName.equals("ContactElectronicMailAddress")) {
            this.contactElectronicMailAddress = ((TextModel) value).getValue();
        } else if (keyName.equals("ContactPersonPrimary")) {
            WmsContactPersonPrimary contactPersonPrimary = (WmsContactPersonPrimary) value;
            this.contactPerson = contactPersonPrimary.person;
            this.contactOrganization = contactPersonPrimary.organization;
        } else if (keyName.equals("ContactAddress")) {
            this.contactAddress = (WmsAddress) value;
        }
    }
}
