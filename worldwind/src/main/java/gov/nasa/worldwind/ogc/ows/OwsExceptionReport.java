/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.ows;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsExceptionReport extends XmlModel {

    protected List<OwsException> exceptions = new ArrayList<>();

    protected String version;

    protected String lang;

    public OwsExceptionReport() {
    }

    public List<OwsException> getExceptions() {
        return exceptions;
    }

    public String getVersion() {
        return version;
    }

    public String getLang() {
        return lang;
    }

    @Override
    public String toString() {
        return "OwsExceptionReport{" +
            "exceptions=" + exceptions +
            ", version='" + version + '\'' +
            ", lang='" + lang + '\'' +
            '}';
    }

    public String toPrettyString() {
        if (exceptions.size() == 0) {
            return null;
        } else if (exceptions.size() == 1) {
            return exceptions.get(0).toPrettyString();
        } else {
            StringBuilder sb = new StringBuilder();
            int ordinal = 1;
            for (OwsException exception : exceptions) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(ordinal++).append(": ");
                sb.append(exception.toPrettyString());
            }
            return sb.toString();
        }
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);

        switch (keyName) {
            case "Exception":
                exceptions.add((OwsException) value);
                break;
            case "version":
                version = (String) value;
                break;
            case "lang":
                lang = (String) value;
                break;
        }
    }
}
