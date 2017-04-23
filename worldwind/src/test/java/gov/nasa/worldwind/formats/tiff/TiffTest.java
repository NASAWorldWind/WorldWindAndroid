/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.formats.tiff;

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
public class TiffTest {

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
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = tiff.getSubfiles().get(0);
        int expected = 512;

        int actualWidth = file.getImageWidth();
        int actualLength = file.getImageLength();

        assertEquals("image width", expected, actualWidth);
        assertEquals("image length", expected, actualLength);
    }

    @Test
    public void testGetOffsets_GeoTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = tiff.getSubfiles().get(0);
        // the first twelve values and the last
        int[] expectedOffsets = {930, 9122, 17314, 25506, 33698, 41890, 50082, 58274, 66466, 74658, 82850, 91042, 517026};

        int[] actualOffsets = file.getStripOffsets();

        // modify the actual offsets to limit the number of test points, didn't want to write in all of the offsets
        int[] modActualOffsets = Arrays.copyOfRange(actualOffsets, 0, 13);
        modActualOffsets[12] = actualOffsets[actualOffsets.length - 1];

        assertTrue("image offsets", Arrays.equals(expectedOffsets, modActualOffsets));
    }

    @Test
    public void testGetDataSize_GeoTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = tiff.getSubfiles().get(0);
        int expectedSize = 524288;

        int actualSize = file.getDataSize();

        assertEquals("image geotiffData size", expectedSize, actualSize);
    }

    @Test
    public void testGetData_Execution_GeoTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = tiff.getSubfiles().get(0);
        ByteBuffer data = ByteBuffer.allocate(file.getDataSize());
        int expectedPosition = file.getDataSize();

        file.getData(data);

        assertEquals("bytebuffer position after geotiffData load", expectedPosition, data.position());
    }

    @Test
    public void testGetBitsPerSample_GeoTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = tiff.getSubfiles().get(0);
        int expectedBitsPerSample = 16;
        int expectedComponentsPerPixel = 1;

        int[] actualBitsPerSample = file.getBitsPerSample();

        assertEquals("bits per sample components", expectedComponentsPerPixel, actualBitsPerSample.length);
        assertEquals("bits per sample values", expectedBitsPerSample, actualBitsPerSample[0]);
    }

    @Test
    public void testGetByteCounts_GeoTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = tiff.getSubfiles().get(0);
        int[] expectedByteCounts = {8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192};

        int[] actualByteCounts = file.getStripByteCounts();

        // modify the actual bytes counts in order to reduce the number of test points
        int[] modActualByteCounts = Arrays.copyOf(actualByteCounts, 13);
        modActualByteCounts[12] = actualByteCounts[actualByteCounts.length - 1];

        assertTrue("byte counts", Arrays.equals(expectedByteCounts, modActualByteCounts));
    }

    @Test
    public void testGetCompression_GeoTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = tiff.getSubfiles().get(0);
        int expectedValue = 1;

        int actualValue = file.getCompression();

        assertEquals("compression type", expectedValue, actualValue);
    }

    @Test
    public void testGetPhotometricInterpretation_GeoTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = tiff.getSubfiles().get(0);
        int expectedValue = 1;

        int actualValue = file.getPhotometricInterpretation();

        assertEquals("photometric interpretation", expectedValue, actualValue);
    }

    @Test
    public void testGetResolutionUnit_GeoTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = tiff.getSubfiles().get(0);
        int expectedValue = 2;

        int actualValue = file.getResolutionUnit();
        assertEquals("resolution unit", expectedValue, actualValue);
    }

    @Test
    public void testGetRowsPerStrip_GeoTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = tiff.getSubfiles().get(0);
        int expectedValue = 8;

        int actualValue = file.getRowsPerStrip();

        assertEquals("rows per strip", expectedValue, actualValue);
    }

    @Test
    public void testGetSampleFormat_GeoTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = tiff.getSubfiles().get(0);
        int expectedSampleFormat = 2;
        int expectedComponentsPerPixel = 1;

        int[] actualSampleFormat = file.getSampleFormat();

        assertEquals("sample format components", expectedComponentsPerPixel, actualSampleFormat.length);
        assertEquals("sample format values", expectedSampleFormat, actualSampleFormat[0]);
    }

    @Test
    public void testSamplesPerPixel_GeoTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = tiff.getSubfiles().get(0);
        int expectedValue = 1;

        int actualValue = file.samplesPerPixel;

        assertEquals("samples per pixel", expectedValue, actualValue);
    }

    @Test
    public void testGetResolutions_GeoTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.geotiffData));
        Subfile file = tiff.getSubfiles().get(0);
        double delta = 1e-9;
        double expectedValue = 72.0;

        double xResolution = file.getXResolution();
        double yResolution = file.getYResolution();

        assertEquals("x resolution", expectedValue, xResolution, delta);
        assertEquals("y resolution", expectedValue, yResolution, delta);
    }

    @Test
    public void testImageWidthAndLength_BlendTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = tiff.getSubfiles().get(0);
        int expectedWidth = 640;
        int expectedLength = 400;

        int actualWidth = file.getImageWidth();
        int actualLength = file.getImageLength();

        assertEquals("image width", expectedWidth, actualWidth);
        assertEquals("image length", expectedLength, actualLength);
    }

    @Test
    public void testGetOffsets_BlendTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = tiff.getSubfiles().get(0);
        int[] expectedOffsets = {8, 40968, 81928, 122888, 163848, 204808, 245768};

        int[] actualOffsets = file.getStripOffsets();

        assertTrue("image offsets", Arrays.equals(expectedOffsets, actualOffsets));
    }

    @Test
    public void testGetDataSize_BlendTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = tiff.getSubfiles().get(0);
        int expectedSize = 256000;

        int actualSize = file.getDataSize();

        assertEquals("image geotiffData size", expectedSize, actualSize);
    }

    @Test
    public void testGetData_Execution_BlendTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = tiff.getSubfiles().get(0);
        ByteBuffer data = ByteBuffer.allocate(file.getDataSize());
        int expectedPosition = file.getDataSize();

        file.getData(data);

        assertEquals("bytebuffer position after geotiffData load", expectedPosition, data.position());
    }

    @Test
    public void testGetBitsPerSample_BlendTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = tiff.getSubfiles().get(0);
        int expectedBitsPerSample = 8;
        int expectedComponentsPerPixel = 1;

        int[] actualBitsPerSample = file.getBitsPerSample();

        assertEquals("bits per sample components", expectedComponentsPerPixel, actualBitsPerSample.length);
        assertEquals("bits per sample values", expectedBitsPerSample, actualBitsPerSample[0]);
    }

    @Test
    public void testGetByteCounts_BlendTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = tiff.getSubfiles().get(0);
        int[] expectedByteCounts = {40960, 40960, 40960, 40960, 40960, 40960, 10240};

        int[] actualByteCounts = file.getStripByteCounts();

        assertTrue("byte counts", Arrays.equals(expectedByteCounts, actualByteCounts));
    }

    @Test
    public void testGetCompression_BlendTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = tiff.getSubfiles().get(0);
        int expectedValue = 1;

        int actualValue = file.getCompression();

        assertEquals("compression type", expectedValue, actualValue);
    }

    @Test
    public void testGetPhotometricInterpretation_BlendTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = tiff.getSubfiles().get(0);
        int expectedValue = 1;

        int actualValue = file.getPhotometricInterpretation();

        assertEquals("photometric interpretation", expectedValue, actualValue);
    }

    @Test
    public void testGetResolutionUnit_BlendTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = tiff.getSubfiles().get(0);
        int expectedValue = 2;

        int actualValue = file.getResolutionUnit();
        assertEquals("resolution unit", expectedValue, actualValue);
    }

    @Test
    public void testGetRowsPerStrip_BlendTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = tiff.getSubfiles().get(0);
        int expectedValue = 64;

        int actualValue = file.getRowsPerStrip();

        assertEquals("rows per strip", expectedValue, actualValue);
    }

    @Test
    public void testGetSampleFormat_BlendTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = tiff.getSubfiles().get(0);
        int expectedSampleFormat = 1;
        int expectedComponentsPerPixel = 1;

        int[] actualSampleFormat = file.getSampleFormat();

        assertEquals("sample format components", expectedComponentsPerPixel, actualSampleFormat.length);
        assertEquals("sample format values", expectedSampleFormat, actualSampleFormat[0]);
    }

    @Test
    public void testSamplesPerPixel_BlendTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = tiff.getSubfiles().get(0);
        int expectedValue = 1;

        int actualValue = file.samplesPerPixel;

        assertEquals("samples per pixel", expectedValue, actualValue);
    }

    @Test
    public void testGetResolutions_BlendTiff() throws Exception {
        Tiff tiff = new Tiff(ByteBuffer.wrap(this.blendtiffData));
        Subfile file = tiff.getSubfiles().get(0);
        double delta = 1e-9;
        double expectedValue = 96.0;

        double xResolution = file.getXResolution();
        double yResolution = file.getYResolution();

        assertEquals("x resolution", expectedValue, xResolution, delta);
        assertEquals("y resolution", expectedValue, yResolution, delta);
    }

    /**
     * Tiff 6.0 provides two mechanisms for storing chunked imagery data, strips and tiles. This test ensures the {@link
     * Subfile}s method for converting the individual tiles to a single image buffer is functioning properly. The test
     * uses a 3x3 grid of 16x16 pixels with 3 samples per pixel and 8 bits per sample. The tiles are stored in tiles
     * and referenced by the tileOffset field. For this test, the original tiles use byte values indicating their tile
     * index in the tileOffset array. This method also tests that overlap of a tile past the image border is considered.
     *
     * @throws Exception
     */
    @Test
    public void testTileCombination() throws Exception {
        ByteBuffer raw = ByteBuffer.allocate(6912);
        raw.put((byte) 'M');
        raw.put((byte) 'M');
        raw.putShort((short) 42);
        Subfile file = new Subfile();
        Tiff tiff = new Tiff(raw);
        raw.clear();
        tiff.buffer = raw;
        file.tiff = tiff;
        file.tileWidth = 16;
        file.tileLength = 16;
        file.imageWidth = 40;
        file.imageLength = 40;
        file.samplesPerPixel = 3;
        file.bitsPerSample = new int[]{8, 8, 8};
        // canned continuous offsets
        file.tileOffsets = new int[]{0, 768, 768 * 2, 768 * 3, 768 * 4, 768 * 5, 768 * 6, 768 * 7, 768 * 8};
        for (int bOffset = 0; bOffset < file.tileOffsets.length; bOffset++) {
            byte[] bytes = new byte[768];
            // each chunk of tiles should use the value of their index
            Arrays.fill(bytes, (byte) bOffset);
            raw.put(bytes, 0, 768);
        }
        ByteBuffer result = ByteBuffer.allocate(6912);
        byte expectedTile0 = 0;
        byte expectedTile1 = 1;
        byte expectedTile2 = 2;
        byte expectedTile3 = 3;
        byte expectedTile4 = 4;
        byte expectedTile5 = 5;
        byte expectedTile6 = 6;
        byte expectedTile7 = 7;
        byte expectedTile8 = 8;

        file.combineTiles(result);
        // sample the result 37.5% through the tile in each tile of the grid
        byte actualTile0 = result.get((6 + 16 * 0 + (6 + 16 * 0) * 40) * 3);
        byte actualTile1 = result.get((6 + 16 * 1 + (6 + 16 * 0) * 40) * 3);
        byte actualTile2 = result.get((6 + 16 * 2 + (6 + 16 * 0) * 40) * 3);
        byte actualTile3 = result.get((6 + 16 * 0 + (6 + 16 * 1) * 40) * 3);
        byte actualTile4 = result.get((6 + 16 * 1 + (6 + 16 * 1) * 40) * 3);
        byte actualTile5 = result.get((6 + 16 * 2 + (6 + 16 * 1) * 40) * 3);
        byte actualTile6 = result.get((6 + 16 * 0 + (6 + 16 * 2) * 40) * 3);
        byte actualTile7 = result.get((6 + 16 * 1 + (6 + 16 * 2) * 40) * 3);
        byte actualTile8 = result.get((6 + 16 * 2 + (6 + 16 * 2) * 40) * 3);

        assertEquals("tile 0 value", expectedTile0, actualTile0);
        assertEquals("tile 1 value", expectedTile1, actualTile1);
        assertEquals("tile 2 value", expectedTile2, actualTile2);
        assertEquals("tile 3 value", expectedTile3, actualTile3);
        assertEquals("tile 4 value", expectedTile4, actualTile4);
        assertEquals("tile 5 value", expectedTile5, actualTile5);
        assertEquals("tile 6 value", expectedTile6, actualTile6);
        assertEquals("tile 7 value", expectedTile7, actualTile7);
        assertEquals("tile 8 value", expectedTile8, actualTile8);
    }
}
