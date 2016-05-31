/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globe.Globe;

public interface Movable {

    /**
     * A position associated with the object that indicates its aggregate geographic position. The chosen position
     * varies among implementers of this interface. For objects defined by a list of positions, the reference position
     * is typically the first position in the list. For symmetric objects the reference position is often the center of
     * the object. In many cases the object's reference position may be explicitly specified by the application.
     *
     * @return the object's reference position, or null if no reference position is available.
     */
    Position getReferencePosition();

    /**
     * Move the shape over the globe's surface while maintaining its original azimuth, its orientation relative to
     * North.
     *
     * @param globe    the globe on which to move the shape.
     * @param position the new position of the shape's reference position.
     */
    void moveTo(Globe globe, Position position);
}
