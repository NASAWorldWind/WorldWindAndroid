/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

precision mediump float;

uniform sampler2D texSampler;

varying vec2 texCoord[2];

void main() {
    /* Using the second texture coordinate, compute a mask that's 1.0 when the fragment is inside the surface tile, and
       0.0 otherwise. */
    float sMask = step(0.0, texCoord[1].s) * (1.0 - step(1.0, texCoord[1].s));
    float tMask = step(0.0, texCoord[1].t) * (1.0 - step(1.0, texCoord[1].t));
    float tileMask = sMask * tMask;

    /* Return the surface tile's 2D texture color using the first texture coordinate. Modulate by the mask to suppress
       fragments outside the surface tile. */
    gl_FragColor = texture2D(texSampler, texCoord[0]) * tileMask;
}