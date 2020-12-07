/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

uniform mediump vec3 lightDirection;/* The light direction vector is used in both shaders, so we must use a common precision. */
uniform float g;
uniform float g2;

varying vec3 primaryColor;
varying vec3 secondaryColor;
varying vec3 direction;

void main () {
    float cos = dot(lightDirection, direction) / length(direction);
    float miePhase = 1.5 * ((1.0 - g2) / (2.0 + g2)) * (1.0 + cos*cos) / pow(1.0 + g2 - 2.0*g*cos, 1.5);
    vec3 color = primaryColor + secondaryColor * miePhase;
    gl_FragColor = vec4(color * color.b, color.b);
}
