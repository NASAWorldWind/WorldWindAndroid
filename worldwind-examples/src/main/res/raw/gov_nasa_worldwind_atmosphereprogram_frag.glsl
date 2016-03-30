/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

precision mediump float;
precision mediump int;

const int FRAGMODE_SKY = 1;
const int FRAGMODE_GROUND_PRIMARY = 2;
const int FRAGMODE_GROUND_SECONDARY = 3;
const int FRAGMODE_GROUND_PRIMARY_TEX_BLEND = 4;

uniform int fragMode;
uniform sampler2D texSampler;
uniform vec3 lightDirection;
uniform float g;
uniform float g2;

varying vec3 primaryColor;
varying vec3 secondaryColor;
varying vec3 direction;
varying vec2 texCoord;

void main () {
    if (fragMode == FRAGMODE_SKY) {
        float cos = dot(lightDirection, direction) / length(direction);
        float miePhase = 1.5 * ((1.0 - g2) / (2.0 + g2)) * (1.0 + cos*cos) / pow(1.0 + g2 - 2.0*g*cos, 1.5);
        vec3 color = primaryColor + secondaryColor * miePhase;
        gl_FragColor = vec4(color * color.b, color.b);
    } else if (fragMode == FRAGMODE_GROUND_PRIMARY) {
        gl_FragColor = vec4(primaryColor, 1.0);
    } else if (fragMode == FRAGMODE_GROUND_SECONDARY) {
        gl_FragColor = vec4(secondaryColor, 1.0);
    } else if (fragMode == FRAGMODE_GROUND_PRIMARY_TEX_BLEND) {
        vec4 texColor = texture2D(texSampler, texCoord);
        gl_FragColor = vec4(primaryColor + texColor.rgb * (1.0 - secondaryColor), 1.0);
    } else {
        gl_FragColor = vec4(1.0);
    }
}
