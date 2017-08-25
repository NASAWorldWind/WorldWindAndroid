/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc;

import gov.nasa.worldwind.ogc.ows.OwsExceptionReport;

public class OgcException extends Exception {

    protected OwsExceptionReport exceptionReport;

    public OgcException() {
    }

    public OgcException(String message) {
        super(message);
    }

    public OgcException(String message, Throwable cause) {
        super(message, cause);
    }

    public OgcException(OwsExceptionReport exceptionReport) {
        super((exceptionReport != null) ? exceptionReport.toPrettyString() : null);
        this.exceptionReport = exceptionReport;
    }

    public OgcException(OwsExceptionReport exceptionReport, Throwable cause) {
        super((exceptionReport != null) ? exceptionReport.toPrettyString() : null, cause);
        this.exceptionReport = exceptionReport;
    }

    public OgcException(Throwable cause) {
        super(cause);
    }

    public OwsExceptionReport getExceptionReport() {
        return exceptionReport;
    }
}
