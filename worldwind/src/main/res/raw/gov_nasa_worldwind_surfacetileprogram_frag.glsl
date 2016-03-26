/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

precision mediump float;

const vec2 zero = vec2(0.0, 0.0);
const vec2 one = vec2(1.0, 1.0);

uniform sampler2D texSampler;

varying vec2 texCoord[2];

void main() {
    /* Modulate the fragment color by a mask that's 1.0 when the fragment is inside the surface tile, and 0.0 otherwise. */
    float texMask = float(all(greaterThanEqual(texCoord[1], zero)) && all(lessThanEqual(texCoord[1], one)));

    /* Return the surface tile's 2D texture color. */
    gl_FragColor = texture2D(texSampler, texCoord[0]) * texMask;
}