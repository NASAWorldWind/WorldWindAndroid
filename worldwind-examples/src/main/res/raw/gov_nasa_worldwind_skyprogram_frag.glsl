/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

precision highp float;

uniform vec3 lightDirection;
uniform float g;
uniform float g2;
uniform float exposure;

varying vec4 primaryColor;
varying vec4 secondaryColor;
varying vec3 direction;
/*varying float depth;*/

void main () {
	float cos = dot(lightDirection, direction) / length(direction);
	float miePhase = 1.5 * ((1.0 - g2) / (2.0 + g2)) * (1.0 + cos*cos) / pow(1.0 + g2 - 2.0*g*cos, 1.5);
    gl_FragColor = primaryColor + secondaryColor * miePhase;
	gl_FragColor.a = gl_FragColor.b;

	/*float cos = dot(lightDirection, direction) / length(direction);
	float rayleighPhase = 0.75 * (1.0 + (cos*cos));
	float miePhase = 1.5 * ((1.0 - g2) / (2.0 + g2)) * (1.0 + cos*cos) / pow(1.0 + g2 - 2.0*g*cos, 1.5);
    float sun = 2.0 * ((1.0 - 0.2) / (2.0 + 0.2)) * (1.0 + cos*cos) / pow(1.0 + 0.2 - 2.0*(-0.2)*cos, 1.0);
    vec4 ambient = (sun * depth) * vec4(0.05, 0.05, 0.1, 1.0);

    vec4 color = (rayleighPhase * primaryColor + miePhase * secondaryColor) + ambient;
    vec4 hdr = 1.0 - exp(color * -exposure);
    float nightmult = clamp(max(hdr.r, max(hdr.g, hdr.b))*1.5, 0.0, 1.0);
    gl_FragColor = hdr;
	gl_FragColor.a = nightmult;*/
}
