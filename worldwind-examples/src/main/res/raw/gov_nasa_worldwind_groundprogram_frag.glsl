/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

precision mediump float;

const int FRAGCOLOR_PRIMARY = 1;
const int FRAGCOLOR_SECONDARY = 2;
const int FRAGCOLOR_PRIMARY_TEX_BLEND = 3;

uniform sampler2D texSampler;
uniform int fragColor;

varying vec4 primaryColor;
varying vec4 secondaryColor;
varying vec2 texCoord;

void main() {
    if (fragColor == FRAGCOLOR_PRIMARY) {
        gl_FragColor = primaryColor;
    } else if (fragColor == FRAGCOLOR_SECONDARY) {
        gl_FragColor = secondaryColor;
    } else if (fragColor == FRAGCOLOR_PRIMARY_TEX_BLEND) {
        vec4 texColor = texture2D(texSampler, texCoord);
        gl_FragColor = primaryColor + texColor * (1.0 - secondaryColor);
    }
}
