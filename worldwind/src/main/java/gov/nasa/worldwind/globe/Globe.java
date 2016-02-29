/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

public interface Globe {

    // TODO could we use a list of elevation models here?

    ElevationModel getElevationModel();

    void setElevationModel(ElevationModel elevationModel);
}
