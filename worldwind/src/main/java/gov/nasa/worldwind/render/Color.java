/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.util.Logger;

/**
 * Represents a red, green, blue, alpha color.
 */
public class Color {

    /**
     * The color white.
     */
    public static final Color WHITE = new Color(1, 1, 1, 1);

    /**
     * The color black.
     */
    public static final Color BLACK = new Color(0, 0, 0, 1);

    /**
     * The color red.
     */
    public static final Color RED = new Color(1, 0, 0, 1);

    /**
     * The color green.
     */
    public static final Color GREEN = new Color(0, 1, 0, 1);

    /**
     * The color blue.
     */
    public static final Color BLUE = new Color(0, 0, 1, 1);

    /**
     * The color cyan.
     */
    public static final Color CYAN = new Color(0, 1, 1, 1);

    /**
     * The color yellow.
     */
    public static final Color YELLOW = new Color(1, 1, 0, 1);

    /**
     * The color magenta.
     */
    public static final Color MAGENTA = new Color(1, 0, 1, 1);

    /**
     * A light gray (75% white).
     */
    public static final Color LIGHT_GRAY = new Color(0.75f, 0.75f, 0.75f, 1f);

    /**
     * A medium gray (50% white).
     */
    public static final Color MEDIUM_GRAY = new Color(0.5f, 0.5f, 0.5f, 1f);

    /**
     * A dark gray (25% white).
     */
    public static final Color DARK_GRAY = new Color(0.25f, 0.25f, 0.25f, 1f);

    /**
     * A transparent color.
     */
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    /**
     * This color's red component, a number between 0 and 1.
     */
    float red;

    /**
     * This color's green component, a number between 0 and 1.
     */
    float green;

    /**
     * This color's blue component, a number between 0 and 1.
     */
    float blue;

    /**
     * This color's alpha component, a number between 0 and 1.
     */
    float alpha;

    /**
     * Constructs a color from red, green, blue and alpha values.
     *
     * @param red   The red component, a number between 0 and 1.
     * @param green The green component, a number between 0 and 1.
     * @param blue  The blue component, a number between 0 and 1.
     * @param alpha The alpha component, a number between 0 and 1.
     */
    public Color(float red, float green, float blue, float alpha) {

        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    /**
     * Copies the components of a specified color to this color.
     *
     * @param color The color to copy.
     *
     * @throws IllegalArgumentException If the specified color is null or undefined.
     */
    public Color(Color color) {
        if (color == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Color", "copy", "missingColor"));
        }

        this.red = color.red;
        this.green = color.green;
        this.blue = color.blue;
        this.alpha = color.alpha;
    }


    /**
     * Assigns the components of this color.
     *
     * @param red   The red component, a number between 0 and 1.
     * @param green The green component, a number between 0 and 1.
     * @param blue  The blue component, a number between 0 and 1.
     * @param alpha The alpha component, a number between 0 and 1.
     *
     * @return This color with the specified components assigned.
     */
    public Color set(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Color color = (Color) o;

        if (Double.compare(color.red, red) != 0) return false;
        if (Double.compare(color.green, green) != 0) return false;
        if (Double.compare(color.blue, blue) != 0) return false;
        return Double.compare(color.alpha, alpha) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(red);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(green);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(blue);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(alpha);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "{" +
            "red=" + red +
            ", green=" + green +
            ", blue=" + blue +
            ", alpha=" + alpha +
            '}';
    }

    public float getRed() {
        return red;
    }

    public float getGreen() {
        return green;
    }

    public float getBlue() {
        return blue;
    }

    public float getAlpha() {
        return alpha;
    }

    /**
     * Returns a string representation of this color, indicating the byte values corresponding to this color's
     * floating-point component values.
     *
     * @return This Color's components as a string.
     */
    public String toByteString() {
        int rb = Math.round(this.red * 255);
        int gb = Math.round(this.green * 255);
        int bb = Math.round(this.blue * 255);
        int ab = Math.round(this.alpha * 255);

        return "(" + rb + "," + gb + "," + bb + "," + ab + ")";
    }

    /**
     * Returns this color's components premultiplied by this color's alpha component.
     *
     * @param array A pre-allocated array in which to return the color components.
     *
     * @return This colors premultiplied components as an array, in the order RGBA.
     */
    public float[] premultipliedComponents(float[] array) {
        array[0] = this.red * this.alpha;
        array[1] = this.green * this.alpha;
        array[2] = this.blue * this.alpha;
        array[3] = this.alpha;

        return array;
    }

    /**
     * Construct a color from an array of color components expressed as byte values.
     *
     * @param bytes A four-element array containing the red, green, blue and alpha color components each in the range
     *              [0, 255];
     *
     * @return The constructed color.
     */
    public static Color fromByteArray(char[] bytes) {
        return new Color(bytes[0] / 255, bytes[1] / 255, bytes[2] / 255, bytes[3] / 255);
    }


    /**
     * Construct a color from specified color components expressed as byte values.
     *
     * @param redByte   The red component in the range [0, 255].
     * @param greenByte The green component in the range [0, 255].
     * @param blueByte  The blue component in the range [0, 255].
     * @param alphaByte The alpha component in the range [0, 255].
     *
     * @return The constructed color.
     */
    public static Color fromBytes(char redByte, char greenByte, char blueByte, char alphaByte) {
        return new Color(redByte / 255, greenByte / 255, blueByte / 255, alphaByte / 255);
    }

    public static Color fromHex(String color) {
        char red = (char) Integer.parseInt(color.substring(0, 2), 16);
        char green = (char) Integer.parseInt(color.substring(2, 4), 16);
        char blue = (char) Integer.parseInt(color.substring(4, 6), 16);
        char alpha = (char) Integer.parseInt(color.substring(6, 8), 16);
        return Color.fromBytes(red, green, blue, alpha);
    }
//    public static Color fromKmlHex(String color) {
//        color = color.split("").reverse().join("");
//        return Color.colorFromHex(color);

//    }


    /**
     * Computes and sets this color to the next higher RBG color. If the color overflows, this color is set to (1 / 255,
     * 0, 0, *), where * indicates the current alpha value.
     *
     * @return This color, set to the next possible color.
     */
    public Color nextColor() {
        int rb = Math.round(this.red * 255);
        int gb = Math.round(this.green * 255);
        int bb = Math.round(this.blue * 255);

        if (rb < 255) {
            this.red = (rb + 1) / 255;
        } else if (gb < 255) {
            this.red = 0;
            this.green = (gb + 1) / 255;
        } else if (bb < 255) {
            this.red = 0;
            this.green = 0;
            this.blue = (bb + 1) / 255;
        } else {
            this.red = 1 / 255;
            this.green = 0;
            this.blue = 0;
        }

        return this;
    }

    public static Color random() {
        return new Color((float) Math.random(), (float) Math.random(), (float) Math.random(), 1);
    }

    /**
     * Indicates whether this color is equal to another color expressed as an array of bytes.
     *
     * @param bytes The red, green, blue and alpha color components.
     *
     * @return true if the colors are equal, otherwise false.
     */
    public boolean equalsBytes(char[] bytes) {
        int rb = Math.round(this.red * 255);
        int gb = Math.round(this.green * 255);
        int bb = Math.round(this.blue * 255);
        int ab = Math.round(this.alpha * 255);

        return rb == bytes[0] && gb == bytes[1] && bb == bytes[2] && ab == bytes[3];
    }


}
