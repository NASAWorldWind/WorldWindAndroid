/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.opengl.GLES20;

import gov.nasa.worldwind.util.Logger;

/**
 * Represents an OpenGL shading language (GLSL) shader program and provides methods for identifying and accessing shader
 * variables. Shader programs are created by instances of this class and made current when the DrawContext.useProgram
 * function is invoked.
 */
public class GpuProgram implements GpuObject {

    /**
     * Indicates the OpenGL program object associated with this GPU program.
     */
    protected int programId;

    /**
     * Indicates the approximate size of the OpenGL resources referenced by this GPU program.
     */
    protected int programSize;

    /**
     * Indicates the OpenGL vertex shader object associated with this GPU program.
     */
    protected int vertexShaderId;

    /**
     * Indicates the OpenGL vertex shader object associated with this GPU program.
     */
    protected int fragmentShaderId;

    /**
     * @param vertexShaderSource
     * @param fragmentShaderSource
     * @param attributeBindings
     */
    public GpuProgram(String vertexShaderSource, String fragmentShaderSource, String[] attributeBindings) { // TODO refactor to accept DrawContext argument
        if (vertexShaderSource == null || fragmentShaderSource == null) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "GpuProgram", "constructor",
                "The shader source is null"));
        }

        int[] status = new int[1];

        int vs = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vs, vertexShaderSource);
        GLES20.glCompileShader(vs);
        GLES20.glGetShaderiv(vs, GLES20.GL_COMPILE_STATUS, status, 0);

        if (status[0] != GLES20.GL_TRUE) {
            String msg = GLES20.glGetShaderInfoLog(vs);
            GLES20.glDeleteShader(vs);
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "GpuProgram", "constructor",
                "Error compiling GL vertex shader \n" + msg));
        }

        int fs = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fs, fragmentShaderSource);
        GLES20.glCompileShader(fs);
        GLES20.glGetShaderiv(vs, GLES20.GL_COMPILE_STATUS, status, 0);

        if (status[0] != GLES20.GL_TRUE) {
            String msg = GLES20.glGetShaderInfoLog(fs);
            GLES20.glDeleteShader(vs);
            GLES20.glDeleteShader(fs);
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "GpuProgram", "constructor",
                "Error compiling GL fragment shader \n" + msg));
        }

        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vs);
        GLES20.glAttachShader(program, fs);

        if (attributeBindings != null) {
            for (int i = 0; i < attributeBindings.length; i++) {
                GLES20.glBindAttribLocation(program, i, attributeBindings[i]);
            }
        }

        GLES20.glLinkProgram(program);
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0);

        if (status[0] != GLES20.GL_TRUE) {
            String msg = GLES20.glGetProgramInfoLog(program);
            GLES20.glDeleteProgram(program);
            GLES20.glDeleteShader(vs);
            GLES20.glDeleteShader(fs);
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "GpuProgram", "constructor",
                "Error linking GL program \n" + msg));
        }

        this.programId = program;
        this.programSize = vertexShaderSource.length() + fragmentShaderSource.length(); // proportional to program complexity
        this.vertexShaderId = vs;
        this.fragmentShaderId = fs;
    }

    @Override
    public int getObjectId() {
        return programId;
    }

    @Override
    public void dispose(DrawContext dc) {
        if (this.programId != 0) {
            GLES20.glDeleteProgram(this.programId);
            GLES20.glDeleteShader(this.vertexShaderId);
            GLES20.glDeleteShader(this.fragmentShaderId);
            this.programId = this.vertexShaderId = this.fragmentShaderId = 0;
        }
    }
}
