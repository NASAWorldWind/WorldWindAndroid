/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import java.util.Map;

public interface MessageListener {

    void onMessage(String name, Object sender, Map<Object, Object> userProperties);
}
