/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

uniform float range;
uniform vec4 color[2];
uniform sampler2D depthSampler;

varying vec4 sightlinePosition;
varying float sightlineDistance;

const vec3 minusOne = vec3(-1.0, -1.0, -1.0);
const vec3 plusOne = vec3(1.0, 1.0, 1.0);

void main() {
    /* Compute a mask that's on when the position is inside the occlusion projection, and off otherwise. Transform the
       position to clip coordinates, where values between -1.0 and 1.0 are in the frustum. */
    vec3 clipCoord = sightlinePosition.xyz / sightlinePosition.w;
    vec3 clipCoordMask = step(minusOne, clipCoord) * step(clipCoord, plusOne);
    float clipMask = clipCoordMask.x * clipCoordMask.y * clipCoordMask.z;

    /* Compute a mask that's on when the position is inside the sightline's range, and off otherwise.*/
    float rangeMask = step(sightlineDistance, range);

    /* Compute a mask that's on when the object's depth is less than the sightline's depth. The depth texture contains
       the scene's minimum depth at each position, from the sightline's point of view. */
    vec3 sightlineCoord = clipCoord * 0.5 + 0.5;
    float sightlineDepth = texture2D(depthSampler, sightlineCoord.xy).z;
    float occludeMask = step(sightlineDepth, sightlineCoord.z);

    /* Modulate the RGBA color with the computed masks to display fragments according to the sightline's configuration. */
    gl_FragColor = mix(color[0], color[1], occludeMask) * clipMask * rangeMask;
}