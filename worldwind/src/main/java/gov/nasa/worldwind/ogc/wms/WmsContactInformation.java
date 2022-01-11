/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsContactInformation extends XmlModel {

    protected String contactPosition;

    protected String contactVoiceTelephone;

    protected String contactFacsimileTelephone;

    protected String contactElectronicMailAddress;

    protected WmsAddress contactAddress;

    protected WmsContactPersonPrimary contactPersonPrimary;

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

    public WmsContactPersonPrimary getContactPersonPrimary() {
        return this.contactPersonPrimary;
    }

    @Override
    public void parseField(String keyName, Object value) {
        switch (keyName) {
            case "ContactPosition":
                this.contactPosition = (String) value;
                break;
            case "ContactVoiceTelephone":
                this.contactVoiceTelephone = (String) value;
                break;
            case "ContactFacsimileNumber":
                this.contactFacsimileTelephone = (String) value;
                break;
            case "ContactElectronicMailAddress":
                this.contactElectronicMailAddress = (String) value;
                break;
            case "ContactPersonPrimary":
                this.contactPersonPrimary = (WmsContactPersonPrimary) value;
                break;
            case "ContactAddress":
                this.contactAddress = (WmsAddress) value;
                break;
        }
    }
}
