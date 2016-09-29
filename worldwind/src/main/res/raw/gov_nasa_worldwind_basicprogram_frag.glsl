/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

precision mediump float;

uniform bool enablePickMode;
uniform bool enableTexture;
uniform vec4 color;
uniform sampler2D texSampler;

varying vec2 texCoord;

void main() {
    /* TODO consolidate pickMode and enableTexture into a single textureMode */
    /* TODO it's confusing that pickMode must be disabled during surface shape render-to-texture */
    if (enablePickMode && enableTexture) {
        /* Modulate the RGBA color with the 2D texture's Alpha component (rounded to 0.0 or 1.0). */
        float texMask = floor(texture2D(texSampler, texCoord).a + 0.5);
        gl_FragColor = color * texMask;
    } else if (!enablePickMode && enableTexture) {
        /* Modulate the RGBA color with the 2D texture's RGBA color. */
        gl_FragColor = color * texture2D(texSampler, texCoord);
    } else {
        /* Return the RGBA color as-is. */
        gl_FragColor = color;
    }
}