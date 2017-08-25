/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.ows;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.XmlModelParser;

public class OwsXmlParser extends XmlModelParser {

    protected String ows20Namespace = "http://www.opengis.net/ows/2.0";

    protected String xmlNamespace = "http://www.w3.org/2001/XMLSchema";

    public OwsXmlParser() {
        registerOws20Models(ows20Namespace);
        registerXmlModels(xmlNamespace);
    }

    public static Object parse(InputStream inputStream) throws IOException, XmlPullParserException {
        XmlPullParser pullParser = Xml.newPullParser();
        pullParser.setInput(inputStream, null /*inputEncoding*/);

        XmlModelParser modelParser = new OwsXmlParser();
        modelParser.setPullParser(pullParser);

        return modelParser.parse();
    }

    public static OwsExceptionReport parseErrorStream(URLConnection connection) {
        InputStream errorStream = null;
        try {
            if (!(connection instanceof HttpURLConnection)) {
                return null; // need an HTTP connection to parse the error stream
            }

            errorStream = ((HttpURLConnection) connection).getErrorStream();
            if (errorStream == null) {
                return null; // connection did not respond with an error
            }

            Object responseXml = parse(errorStream);
            return (responseXml instanceof OwsExceptionReport) ? (OwsExceptionReport) responseXml : null;
        } catch (RuntimeException rethrown) {
            throw rethrown;
        } catch (Exception ignored) {
            return null; // silently ignore checked exceptions
        } finally {
            WWUtil.closeSilently(errorStream);
        }
    }

    protected void registerOws20Models(String namespace) {
        registerXmlModel(namespace, "Exception", OwsException.class);
        registerTxtModel(namespace, "exceptionCode");
        registerXmlModel(namespace, "ExceptionReport", OwsExceptionReport.class);
        registerTxtModel(namespace, "ExceptionText");
        registerTxtModel(namespace, "locator");
        registerTxtModel(namespace, "version");
    }

    protected void registerXmlModels(String namespace) {
        registerTxtModel(namespace, "lang");
    }
}
