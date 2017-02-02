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
        if (keyName.equals("ContactPosition")) {
            this.contactPosition = (String) value;
        } else if (keyName.equals("ContactVoiceTelephone")) {
            this.contactVoiceTelephone = (String) value;
        } else if (keyName.equals("ContactFacsimileNumber")) {
            this.contactFacsimileTelephone = (String) value;
        } else if (keyName.equals("ContactElectronicMailAddress")) {
            this.contactElectronicMailAddress = (String) value;
        } else if (keyName.equals("ContactPersonPrimary")) {
            this.contactPersonPrimary = (WmsContactPersonPrimary) value;
        } else if (keyName.equals("ContactAddress")) {
            this.contactAddress = (WmsAddress) value;
        }
    }
}
