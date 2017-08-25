/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.ows;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsException extends XmlModel {

    protected List<String> exceptionText = new ArrayList<>();

    protected String exceptionCode;

    protected String locator;

    public OwsException() {
    }

    public List<String> getExceptionText() {
        return exceptionText;
    }

    public String getExceptionCode() {
        return exceptionCode;
    }

    public String getLocator() {
        return locator;
    }

    @Override
    public String toString() {
        return "OwsException{" +
            "exceptionText=" + exceptionText +
            ", exceptionCode='" + exceptionCode + '\'' +
            ", locator='" + locator + '\'' +
            '}';
    }

    public String toPrettyString() {
        if (exceptionText.size() == 0) {
            return null;
        } else if (exceptionText.size() == 1) {
            return exceptionText.get(0);
        } else {
            StringBuilder sb = new StringBuilder();
            for (String text : exceptionText) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(text);
            }
            return sb.toString();
        }
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);

        switch (keyName) {
            case "ExceptionText":
                exceptionText.add((String) value);
                break;
            case "exceptionCode":
                exceptionCode = (String) value;
                break;
            case "locator":
                locator = (String) value;
                break;
        }
    }
}
