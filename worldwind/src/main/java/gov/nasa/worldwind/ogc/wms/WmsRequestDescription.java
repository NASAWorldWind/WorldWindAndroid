/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.XmlModel;
import gov.nasa.worldwind.util.xml.XmlPullParserContext;

public class WmsRequestDescription extends XmlModel {

    protected QName format;

    protected QName dcpType;

    protected String requestName;

    public WmsRequestDescription(String namespaceURI) {
        super(namespaceURI);

        this.initialize();
    }

    private void initialize() {
        this.format = new QName(this.getNamespaceUri(), "Format");
        this.dcpType = new QName(this.getNamespaceUri(), "DCPType");
    }

    public Object read(XmlPullParserContext ctx) throws XmlPullParserException, IOException {

        // Use the name of the element to define the name of this description
        this.setRequestName(ctx.getParser().getName());

        return super.read(ctx);
    }

    public WmsOnlineResource getOnlineResource(String requestMethod) {
        for (WmsDcpType dct : this.getDcpTypes()) {
            for (WmsDcpType.DcpInfo dcpInfo : dct.getDcpInfos()) {
                if (dcpInfo.method.equals(requestMethod)) {
                    return dcpInfo.onlineResource;
                }
            }
        }

        return null;
    }

    public Set<String> getFormats() {
        return (Set<String>) super.getField(this.format);
    }

    /**
     * This will clear existing formats and replace with the provided set.
     *
     * @param formats
     */
    protected void setFormats(Set<String> formats) {
        Set<String> currentFormats = (Set<String>) super.getField(this.format);
        if (currentFormats == null) {
            currentFormats = new HashSet<>();
            super.setField(this.format, currentFormats);
        }

        currentFormats.clear();
        currentFormats.addAll(formats);
    }

    protected void addFormat(String format) {
        this.setField(this.format, format);
    }

    /**
     * This will clear existing DCP Types and replace with the provided set.
     *
     * @param dcTypes
     */
    protected void setDCPTypes(Set<WmsDcpType> dcTypes) {
        Set<WmsDcpType> currentDcpTypes = (Set<WmsDcpType>) super.getField(this.dcpType);
        if (currentDcpTypes == null) {
            currentDcpTypes = new HashSet<>();
            super.setField(this.dcpType, currentDcpTypes);
        }

        currentDcpTypes.clear();
        currentDcpTypes.addAll(dcTypes);
    }

    public Set<WmsDcpType> getDcpTypes() {
        return (Set<WmsDcpType>) super.getField(this.dcpType);
    }

    public void addDcpType(WmsDcpType dct) {
        this.setField(this.dcpType, dct);
    }

    public String getRequestName() {
        return requestName;
    }

    protected void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    @Override
    public void setField(QName keyName, Object value) {

        // Check if this is a format element
        if (keyName.equals(this.format)) {
            // Formats are stored as a set
            Set<String> formats = (Set<String>) super.getField(keyName);
            if (formats == null) {
                formats = new HashSet<>();
                super.setField(keyName, formats);
            }

            if (value instanceof String) {
                formats.add((String) value);
            } else if (value instanceof XmlModel) {
                formats.add(((XmlModel) value).getField(XmlModel.CHARACTERS_CONTENT).toString());
            } else {
                super.setField(keyName, value);
            }
        } else if (keyName.equals(this.dcpType)) {
            // DCP Types are stored as a set
            Set<WmsDcpType> dcpTypes = (Set<WmsDcpType>) super.getField(this.dcpType);
            if (dcpTypes == null) {
                dcpTypes = new HashSet<>();
                super.setField(keyName, dcpTypes);
            }

            if (value instanceof WmsDcpType) {
                dcpTypes.add((WmsDcpType) value);
            } else {
                super.setField(keyName, value);
            }
        } else {
            super.setField(keyName, value);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (this.getRequestName() != null)
            sb.append(this.getRequestName()).append("\n");

        sb.append("\tFormats: ");
        for (String format : this.getFormats()) {
            sb.append("\t").append(format).append(", ");
        }

        sb.append("\n\tDCPTypes:\n");
        for (WmsDcpType dcpt : this.getDcpTypes()) {
            sb.append("\t\t").append(dcpt.toString()).append("\n");
        }

        return sb.toString();
    }
}
