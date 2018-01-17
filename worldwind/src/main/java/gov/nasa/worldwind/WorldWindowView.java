/*
 * Copyright (c) 2018 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import gov.nasa.worldwind.geom.Matrix4;

public interface WorldWindowView {

    Matrix4 viewingMatrix(WorldWindow wwd);
}
