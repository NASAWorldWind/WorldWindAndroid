/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

precision mediump float;

uniform vec4 color;
uniform sampler2D texSampler;
uniform bool enableTexture;

varying vec2 texCoord;

void main() {
    if (enableTexture) {
        gl_FragColor = color * texture2D(texSampler, texCoord);
    } else {
        gl_FragColor = color;
    }
}