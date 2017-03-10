/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsOperationsMetadata extends XmlModel {

    protected List<OwsOperation> operations = new ArrayList<>();

    public OwsOperationsMetadata() {
    }

    public List<OwsOperation> getOperations() {
        return this.operations;
    }

    public OwsOperation getGetCapabilities() {
        for (OwsOperation operation : this.operations) {
            if (operation.getName().equals("GetCapabilities")) {
                return operation;
            }
        }

        return null;
    }

    public OwsOperation getGetTile() {
        for (OwsOperation operation : this.operations) {
            if (operation.getName().equals("GetTile")) {
                return operation;
            }
        }

        return null;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Operation")) {
            this.operations.add((OwsOperation) value);
        }
    }
}
