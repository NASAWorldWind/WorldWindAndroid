/*
 * Copyright (c) 2018 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.WorldWindow;

public interface ViewingTransform {

    // Considerations

    // 1) Name

    // 2) Roll required to unambiguously convert between disparate types. But, since we can convert like-to-like
    // directly, do we really care?

    // 3) What is enough information to compute a viewing matrix?

    Matrix4 viewingMatrix(WorldWindow wwd);
}
