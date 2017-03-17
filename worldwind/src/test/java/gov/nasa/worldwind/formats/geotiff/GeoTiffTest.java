/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.formats.geotiff;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import gov.nasa.worldwind.util.Logger;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class)   // We mock the Logger class to avoid its calls to android.util.log
public class GeoTiffTest {

    protected byte[] geotiffData;

    protected byte[] blendtiffData;

    @Before
    public void setup() throws Exception {
        String resourceName = "test_gov_nasa_worldwind_geotiff.tif";
        this.geotiffData = this.setupData(resourceName);
        resourceName = "test_gov_nasa_worldwind_blend.tif";
        this.blendtiffData = this.setupData(resourceName);
    }

    protected byte[] setupData(String resourceName) throws Exception {
        BufferedInputStream inputStream = new BufferedInputStream(this.getClass().getClassLoader().getResourceAsStream(resourceName));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int nRead;
        byte[] dataBuffer = new byte[4096];

        while ((nRead = inputStream.read(dataBuffer, 0, dataBuffer.length)) != -1) {
            baos.write(dataBuffer, 0, nRead);
        }

        baos.flush();

        return baos.toByteArray();
    }

    @Test
    public void testImageWidthAndLength_GeoTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        int expected = 512;

        int actualWidth = file.getImageWidth();
        int actualLength = file.getImageLength();

        assertEquals("image width", expected, actualWidth);
        assertEquals("image length", expected, actualLength);
    }

    @Test
    public void testGetOffsets_GeoTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        // the first twelve values and the last
        int[] expectedOffsets = {930, 9122, 17314, 25506, 33698, 41890, 50082, 58274, 66466, 74658, 82850, 91042, 517026};

        int[] actualOffsets = file.getOffsets();

        // modify the actual offsets to limit the number of test points, didn't want to write in all of the offsets
        int[] modActualOffsets = Arrays.copyOfRange(actualOffsets, 0, 13);
        modActualOffsets[12] = actualOffsets[actualOffsets.length - 1];

        assertTrue("image offsets", Arrays.equals(expectedOffsets, modActualOffsets));
    }

    @Test
    public void testGetDataSize_GeoTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        int expectedSize = 524288;

        int actualSize = file.getDataSize();

        assertEquals("image geotiffData size", expectedSize, actualSize);
    }

    @Test
    public void testGetData_Execution_GeoTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        ByteBuffer data = ByteBuffer.allocate(file.getDataSize());
        int expectedPosition = file.getDataSize();

        file.getData(data);

        assertEquals("bytebuffer position after geotiffData load", expectedPosition, data.position());
    }

    @Test
    public void testGetBitsPerSample_GeoTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        int expectedBitsPerSample = 16;
        int expectedComponentsPerPixel = 1;

        int[] actualBitsPerSample = file.getBitsPerSample();

        assertEquals("bits per sample components", expectedComponentsPerPixel, actualBitsPerSample.length);
        assertEquals("bits per sample values", expectedBitsPerSample, actualBitsPerSample[0]);
    }

    @Test
    public void testGetByteCounts_GeoTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        int[] expectedByteCounts = {8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192};

        int[] actualByteCounts = file.getByteCounts();

        // modify the actual bytes counts in order to reduce the number of test points
        int[] modActualByteCounts = Arrays.copyOf(actualByteCounts, 13);
        modActualByteCounts[12] = actualByteCounts[actualByteCounts.length - 1];

        assertTrue("byte counts", Arrays.equals(expectedByteCounts, modActualByteCounts));
    }

    @Test
    public void testGetCompression_GeoTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        int expectedValue = 1;

        int actualValue = file.getCompression();

        assertEquals("compression type", expectedValue, actualValue);
    }

    @Test
    public void testGetPhotometricInterpretation_GeoTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        int expectedValue = 1;

        int actualValue = file.getPhotometricInterpretation();

        assertEquals("photometric interpretation", expectedValue, actualValue);
    }

    @Test
    public void testGetResolutionUnit_GeoTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        int expectedValue = 2;

        int actualValue = file.getResolutionUnit();
        assertEquals("resolution unit", expectedValue, actualValue);
    }

    @Test
    public void testGetRowsPerStrip_GeoTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        int expectedValue = 8;

        int actualValue = file.getRowsPerStrip();

        assertEquals("rows per strip", expectedValue, actualValue);
    }

    @Test
    public void testGetSampleFormat_GeoTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        int expectedSampleFormat = 2;
        int expectedComponentsPerPixel = 1;

        int[] actualSampleFormat = file.getSampleFormat();

        assertEquals("sample format components", expectedComponentsPerPixel, actualSampleFormat.length);
        assertEquals("sample format values", expectedSampleFormat, actualSampleFormat[0]);
    }

    @Test
    public void testSamplesPerPixel_GeoTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        int expectedValue = 1;

        int actualValue = file.samplesPerPixel;

        assertEquals("samples per pixel", expectedValue, actualValue);
    }

    @Test
    public void testGetResolutions_GeoTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        double delta = 1e-9;
        double expectedValue = 72.0;

        double xResolution = file.getXResolution();
        double yResolution = file.getYResolution();

        assertEquals("x resolution", expectedValue, xResolution, delta);
        assertEquals("y resolution", expectedValue, yResolution, delta);
    }

    @Test
    public void testImageWidthAndLength_BlendTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        int expectedWidth = 640;
        int expectedLength = 400;

        int actualWidth = file.getImageWidth();
        int actualLength = file.getImageLength();

        assertEquals("image width", expectedWidth, actualWidth);
        assertEquals("image length", expectedLength, actualLength);
    }

    @Test
    public void testGetOffsets_BlendTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        int[] expectedOffsets = {8, 40968, 81928, 122888, 163848, 204808, 245768};

        int[] actualOffsets = file.getOffsets();

        assertTrue("image offsets", Arrays.equals(expectedOffsets, actualOffsets));
    }

    @Test
    public void testGetDataSize_BlendTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        int expectedSize = 256000;

        int actualSize = file.getDataSize();

        assertEquals("image geotiffData size", expectedSize, actualSize);
    }

    @Test
    public void testGetData_Execution_BlendTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        ByteBuffer data = ByteBuffer.allocate(file.getDataSize());
        int expectedPosition = file.getDataSize();

        file.getData(data);

        assertEquals("bytebuffer position after geotiffData load", expectedPosition, data.position());
    }

    @Test
    public void testGetBitsPerSample_BlendTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        int expectedBitsPerSample = 8;
        int expectedComponentsPerPixel = 1;

        int[] actualBitsPerSample = file.getBitsPerSample();

        assertEquals("bits per sample components", expectedComponentsPerPixel, actualBitsPerSample.length);
        assertEquals("bits per sample values", expectedBitsPerSample, actualBitsPerSample[0]);
    }

    @Test
    public void testGetByteCounts_BlendTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        int[] expectedByteCounts = {40960, 40960, 40960, 40960, 40960, 40960, 10240};

        int[] actualByteCounts = file.getByteCounts();

        assertTrue("byte counts", Arrays.equals(expectedByteCounts, actualByteCounts));
    }

    @Test
    public void testGetCompression_BlendTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        int expectedValue = 1;

        int actualValue = file.getCompression();

        assertEquals("compression type", expectedValue, actualValue);
    }

    @Test
    public void testGetPhotometricInterpretation_BlendTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        int expectedValue = 1;

        int actualValue = file.getPhotometricInterpretation();

        assertEquals("photometric interpretation", expectedValue, actualValue);
    }

    @Test
    public void testGetResolutionUnit_BlendTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        int expectedValue = 2;

        int actualValue = file.getResolutionUnit();
        assertEquals("resolution unit", expectedValue, actualValue);
    }

    @Test
    public void testGetRowsPerStrip_BlendTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        int expectedValue = 64;

        int actualValue = file.getRowsPerStrip();

        assertEquals("rows per strip", expectedValue, actualValue);
    }

    @Test
    public void testGetSampleFormat_BlendTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        int expectedSampleFormat = 1;
        int expectedComponentsPerPixel = 1;

        int[] actualSampleFormat = file.getSampleFormat();

        assertEquals("sample format components", expectedComponentsPerPixel, actualSampleFormat.length);
        assertEquals("sample format values", expectedSampleFormat, actualSampleFormat[0]);
    }

    @Test
    public void testSamplesPerPixel_BlendTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        int expectedValue = 1;

        int actualValue = file.samplesPerPixel;

        assertEquals("samples per pixel", expectedValue, actualValue);
    }

    @Test
    public void testGetResolutions_BlendTiff() throws Exception {
        GeoTiff geoTiff = new GeoTiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = geoTiff.getSubfiles().get(0);
        double delta = 1e-9;
        double expectedValue = 96.0;

        double xResolution = file.getXResolution();
        double yResolution = file.getYResolution();

        assertEquals("x resolution", expectedValue, xResolution, delta);
        assertEquals("y resolution", expectedValue, yResolution, delta);
    }
}
