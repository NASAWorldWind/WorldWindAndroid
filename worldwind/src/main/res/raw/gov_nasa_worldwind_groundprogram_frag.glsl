/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

precision mediump float;
precision mediump int;

const int FRAGMODE_PRIMARY = 1;
const int FRAGMODE_SECONDARY = 2;
const int FRAGMODE_PRIMARY_TEX_BLEND = 3;

uniform int fragMode;
uniform sampler2D texSampler;

varying vec3 primaryColor;
varying vec3 secondaryColor;
varying vec2 texCoord;

void main () {
    if (fragMode == FRAGMODE_PRIMARY) {
        gl_FragColor = vec4(primaryColor, 1.0);
    } else if (fragMode == FRAGMODE_SECONDARY) {
        gl_FragColor = vec4(secondaryColor, 1.0);
    } else if (fragMode == FRAGMODE_PRIMARY_TEX_BLEND) {
        vec4 texColor = texture2D(texSampler, texCoord);
        gl_FragColor = vec4(primaryColor + texColor.rgb * (1.0 - secondaryColor), 1.0);
    } else {
        gl_FragColor = vec4(1.0); /* return opaque white fragments if fragMode is unrecognized */
    }
}
