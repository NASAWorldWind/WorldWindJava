/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.util.*;

import java.nio.*;
import java.nio.charset.*;

/**
 * @author dcollins
 * @version $Id: VPFBasicDataBufferFactory.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class VPFBasicDataBufferFactory implements VPFDataBufferFactory
{
    public static final short NO_VALUE_SHORT = -32768; // binary: 10000000 00000000
    public static final int NO_VALUE_INT = -2147483648; // binary: 10000000 00000000 00000000 00000000

    public static class NullDataFactory extends VPFBasicDataBufferFactory
    {
        public VPFDataBuffer newDataBuffer(int numRows, int elementsPerRow)
        {
            return new NullDataBuffer();
        }
    }

    public static class DateTimeDataFactory extends VPFBasicDataBufferFactory
    {
        public VPFDataBuffer newDataBuffer(int numRows, int elementsPerRow)
        {
            return new GenericDataBuffer(new DateTimeReader(), numRows);
        }
    }

    public static class TripledIdDataFactory extends VPFBasicDataBufferFactory
    {
        public VPFDataBuffer newDataBuffer(int numRows, int elementsPerRow)
        {
            return new GenericDataBuffer(new TripletIdReader(), numRows);
        }
    }

    public static class TextDataFactory extends VPFBasicDataBufferFactory
    {
        protected String charsetName;

        public TextDataFactory(String charsetName)
        {
            this.charsetName = charsetName;
        }

        public VPFDataBuffer newDataBuffer(int numRows, int elementsPerRow)
        {
            return new TextDataBuffer(this.charsetName, numRows, elementsPerRow);
        }
    }

    public static class ShortDataFactory extends VPFBasicDataBufferFactory
    {
        public VPFDataBuffer newDataBuffer(int numRows, int elementsPerRow)
        {
            return new ScalarDataBuffer(new ShortReader(), new IntAccessor(), new BufferFactory.ShortBufferFactory(),
                numRows);
        }
    }

    public static class IntDataFactory extends VPFBasicDataBufferFactory
    {
        public VPFDataBuffer newDataBuffer(int numRows, int elementsPerRow)
        {
            return new ScalarDataBuffer(new IntReader(), new IntAccessor(), new BufferFactory.IntBufferFactory(),
                numRows);
        }
    }

    public static class FloatDataFactory extends VPFBasicDataBufferFactory
    {
        public VPFDataBuffer newDataBuffer(int numRows, int elementsPerRow)
        {
            return new ScalarDataBuffer(new FloatReader(), new DoubleAccessor(), new BufferFactory.FloatBufferFactory(),
                numRows);
        }
    }

    public static class DoubleDataFactory extends VPFBasicDataBufferFactory
    {
        public VPFDataBuffer newDataBuffer(int numRows, int elementsPerRow)
        {
            return new ScalarDataBuffer(new DoubleReader(), new DoubleAccessor(),
                new BufferFactory.DoubleBufferFactory(), numRows);
        }
    }

    public abstract static class VecDataFactory extends VPFBasicDataBufferFactory
    {
        protected int coordsPerElem;

        public VecDataFactory(int coordsPerElem)
        {
            this.coordsPerElem = coordsPerElem;
        }
    }

    public static class ShortVecDataFactory extends VecDataFactory
    {
        public ShortVecDataFactory(int coordsPerElem)
        {
            super(coordsPerElem);
        }

        public VPFDataBuffer newDataBuffer(int numRows, int elementsPerRow)
        {
            return new VecDataBuffer(
                new ShortVecReader(this.coordsPerElem),
                this.coordsPerElem,
                new BufferFactory.ShortBufferFactory(), numRows, elementsPerRow);
        }
    }

    public static class IntVecDataFactory extends VecDataFactory
    {
        public IntVecDataFactory(int coordsPerElem)
        {
            super(coordsPerElem);
        }

        public VPFDataBuffer newDataBuffer(int numRows, int elementsPerRow)
        {
            return new VecDataBuffer(
                new IntVecReader(this.coordsPerElem),
                this.coordsPerElem,
                new BufferFactory.IntBufferFactory(), numRows, elementsPerRow);
        }
    }

    public static class FloatVecDataFactory extends VecDataFactory
    {
        public FloatVecDataFactory(int coordsPerElem)
        {
            super(coordsPerElem);
        }

        public VPFDataBuffer newDataBuffer(int numRows, int elementsPerRow)
        {
            return new VecDataBuffer(
                new FloatVecReader(this.coordsPerElem),
                this.coordsPerElem,
                new BufferFactory.FloatBufferFactory(), numRows, elementsPerRow);
        }
    }

    public static class DoubleVecDataFactory extends VecDataFactory
    {
        public DoubleVecDataFactory(int coordsPerElem)
        {
            super(coordsPerElem);
        }

        public VPFDataBuffer newDataBuffer(int numRows, int elementsPerRow)
        {
            return new VecDataBuffer(
                new DoubleVecReader(this.coordsPerElem),
                this.coordsPerElem,
                new BufferFactory.DoubleBufferFactory(), numRows, elementsPerRow);
        }
    }

    public static boolean isNoValueShort(short s)
    {
        return s == NO_VALUE_SHORT;
    }

    public static boolean isNoValueInt(int i)
    {
        return i == NO_VALUE_INT;
    }

    public static boolean isNoValueFloat(float f)
    {
        return Float.isNaN(f);
    }

    public static boolean isNoValueDouble(double d)
    {
        return Double.isNaN(d);
    }

    public static boolean isNoValueText(String s)
    {
        if (s == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        switch (s.length())
        {
            case 1:
                return s.equals("-");
            case 2:
                return s.equals("--");
            default:
                return s.equalsIgnoreCase("N/A");
        }
    }

    //**************************************************************//
    //********************  Null Data  *****************************//
    //**************************************************************//

    protected static class NullDataBuffer implements VPFDataBuffer
    {
        public Object get(int index)
        {
            return null;
        }

        public Object getBackingData()
        {
            return null;
        }

        public boolean hasValue(int index)
        {
            return false;
        }

        public void read(ByteBuffer byteBuffer)
        {
        }

        public void read(ByteBuffer byteBuffer, int length)
        {
        }
    }

    //**************************************************************//
    //********************  Generic Data  **************************//
    //**************************************************************//

    protected interface GenericReader
    {
        Object read(ByteBuffer byteBuffer);
    }

    protected static class GenericDataBuffer implements VPFDataBuffer
    {
        protected GenericReader reader;
        protected Object[] array;
        protected int position;

        public GenericDataBuffer(GenericReader reader, int numRows)
        {
            this.reader = reader;
            this.array = new Object[1 + numRows];
            // Start with position 1 that the coordinate N cooresponds to row id N.
            this.position = 1;
        }

        public Object get(int index)
        {
            return this.array[index];
        }

        public Object getBackingData()
        {
            return this.array;
        }

        public boolean hasValue(int index)
        {
            // For Date/Time data, entry is null when space character filled.
            // For Triplet ID data, entry is null when type byte = 0.
            return this.get(index) != null;
        }

        public void read(ByteBuffer byteBuffer)
        {
            Object o = this.reader.read(byteBuffer);
            this.array[this.position] = o;
            this.position++;
        }

        public void read(ByteBuffer byteBuffer, int length)
        {
            // Intentionally ignoring the length parameter. Generic fields can never be variable length.
            this.read(byteBuffer);
        }
    }

    //**************************************************************//
    //********************  Date/Time Data  ************************//
    //**************************************************************//

    protected static class DateTimeReader implements GenericReader
    {
        protected TextReader textReader = new TextReader("US-ASCII");

        public Object read(ByteBuffer byteBuffer)
        {
            // TODO: correct VPF date parsing.

            CharBuffer buffer = this.textReader.read(byteBuffer, 20);
            if (buffer.length() == 0)
                return null;

            //try
            //{
            //    String pattern = makeDatePattern(s);
            //    DateFormat format = new SimpleDateFormat(pattern);
            //    return format.parse(s);
            //}
            //catch (ParseException e)
            //{
            //    String message = Logging.getMessage("generic.ConversionError", s);
            //    Logging.logger().severe(message);
            //}

            return null;
        }

        protected static String makeDatePattern(String dateText)
        {
            StringBuilder sb = new StringBuilder();

            int length = dateText.length();

            sb.append("yyyy");
            if (length > 4)
                sb.append("MM");
            if (length > 6)
                sb.append("dd");

            if (length > 8)
                sb.append("HHmmss");

            if (length > 14)
                sb.append(dateText, 14, 15); // Append the separator character.

            if (length > 16)
                sb.append("ZZZZ");
            else if (length == 16)
                sb.append("Z");

            return sb.toString();
        }
    }

    //**************************************************************//
    //********************  Triplet ID Data  ***********************//
    //**************************************************************//

    protected static class TripletIdReader implements GenericReader
    {
        public Object read(ByteBuffer byteBuffer)
        {
            int type = byteBuffer.get();
            if (type == 0)
                return null;

            int id = readId(byteBuffer, type >> 6);
            int tileId = readId(byteBuffer, type >> 4);
            int extId = readId(byteBuffer, type >> 2);

            return new VPFTripletId(id, tileId, extId);
        }

        protected static int readId(ByteBuffer buffer, int length)
        {
            // Bit Count    ->  Number Bits In Field
            //      0                0
            //      1                8
            //      2                16
            //      3                32

            switch (length & 3)
            {
                case 0:
                    return -1;
                case 1:
                    return 0xFF & buffer.get();
                case 2:
                    return 0xFFFF & buffer.getShort();
                case 3:
                    return buffer.getInt();
                default:
                    return -1;
            }
        }
    }

    //**************************************************************//
    //********************  Text Data  *****************************//
    //**************************************************************//

    protected static class TextReader
    {
        protected CharsetDecoder decoder;

        public TextReader(String charsetName)
        {
            try
            {
                Charset cs = Charset.forName(charsetName);
                this.decoder = cs.newDecoder();
                this.decoder.onMalformedInput(CodingErrorAction.REPLACE);
                this.decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
            }
            catch (IllegalCharsetNameException e)
            {
                String message = Logging.getMessage("generic.InvalidCharsetName", charsetName);
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            catch (UnsupportedCharsetException e)
            {
                String message = Logging.getMessage("generic.InvalidCharsetName", charsetName);
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
        }

        public void read(ByteBuffer byteBuffer, int length, CharBuffer outBuffer)
        {
            int nextPosition = byteBuffer.position() + length;
            int limit = byteBuffer.limit();
            byteBuffer.limit(nextPosition);

            this.decoder.reset();
            this.decoder.decode(byteBuffer, outBuffer, true);
            this.decoder.flush(outBuffer);

            byteBuffer.position(nextPosition);
            byteBuffer.limit(limit);
            outBuffer.flip();
        }

        public CharBuffer read(ByteBuffer byteBuffer, int length)
        {
            CharBuffer charBuffer = CharBuffer.allocate(length);
            this.read(byteBuffer, length, charBuffer);
            return charBuffer;
        }
    }

    protected static class TextDataBuffer implements VPFDataBuffer
    {
        protected int elementsPerRow;
        protected TextReader reader;
        protected CompoundStringBuilder buffer;
        protected CharBuffer tmpBuffer;

        public TextDataBuffer(String charsetName, int numRows, int elementsPerRow)
        {
            int stringLength = Math.max(1, elementsPerRow);

            this.elementsPerRow = elementsPerRow;
            this.reader = new TextReader(charsetName);
            this.buffer = new CompoundStringBuilder(new StringBuilder((1 + numRows) * stringLength), 1 + numRows);
            // Insert an empty string so that the coordinate N cooresponds to row id N.
            this.buffer.append("");
        }

        public Object get(int index)
        {
            String s = this.buffer.substring(index);
            return s != null ? s.trim() : null;
        }

        public Object getBackingData()
        {
            return this.buffer;
        }

        public boolean hasValue(int index)
        {
            // Variable length text is null if it has zero length.
            if (this.elementsPerRow < 0)
            {
                return this.buffer.substringLength(index) > 0;
            }
            // Fixed length text is null if it equals "N/A".
            else
            {
                return !isNoValueText(this.buffer.substring(index).trim());
            }
        }

        public void read(ByteBuffer byteBuffer)
        {
            int length = byteBuffer.getInt();
            this.read(byteBuffer, length);
        }

        public void read(ByteBuffer byteBuffer, int length)
        {
            if (this.tmpBuffer == null || this.tmpBuffer.capacity() < length)
                this.tmpBuffer = WWBufferUtil.newCharBuffer(length, true);

            this.tmpBuffer.clear();
            this.tmpBuffer.limit(length);
            this.reader.read(byteBuffer, length, this.tmpBuffer);
            this.buffer.append(this.tmpBuffer);
        }
    }

    //**************************************************************//
    //********************  Scalar Data  ***************************//
    //**************************************************************//

    protected interface ScalarReader
    {
        double read(ByteBuffer byteBuffer);
    }

    protected interface ScalarAccessor
    {
        Object get(BufferWrapper bufferWrapper, int index);

        boolean hasValue(BufferWrapper bufferWrapper, int index);
    }

    protected static class ShortReader implements ScalarReader
    {
        public double read(ByteBuffer byteBuffer)
        {
            return byteBuffer.getShort();
        }
    }

    protected static class IntReader implements ScalarReader
    {
        public double read(ByteBuffer byteBuffer)
        {
            return byteBuffer.getInt();
        }
    }

    protected static class FloatReader implements ScalarReader
    {
        public double read(ByteBuffer byteBuffer)
        {
            return byteBuffer.getFloat();
        }
    }

    protected static class DoubleReader implements ScalarReader
    {
        public double read(ByteBuffer byteBuffer)
        {
            return byteBuffer.getDouble();
        }
    }

    protected static class ShortAccessor extends IntAccessor
    {
        public boolean hasValue(BufferWrapper bufferWrapper, int index)
        {
            // Scalar shorts are null when equal to "no value" 16 bit pattern.
            return !isNoValueShort((short) bufferWrapper.getInt(index));
        }
    }

    protected static class IntAccessor implements ScalarAccessor
    {
        public Object get(BufferWrapper bufferWrapper, int index)
        {
            return bufferWrapper.getInt(index);
        }

        public boolean hasValue(BufferWrapper bufferWrapper, int index)
        {
            // Scalar ints are null when equal to "no value" 32 bit pattern.
            return !isNoValueInt(bufferWrapper.getInt(index));
        }
    }

    protected static class FloatAccessor extends DoubleAccessor
    {
        public boolean hasValue(BufferWrapper bufferWrapper, int index)
        {
            // Scalar floats are null when equal to the 32 bit floating point NaN.
            return !isNoValueFloat((float) bufferWrapper.getDouble(index));
        }
    }

    protected static class DoubleAccessor implements ScalarAccessor
    {
        public Object get(BufferWrapper bufferWrapper, int index)
        {
            return bufferWrapper.getDouble(index);
        }

        public boolean hasValue(BufferWrapper bufferWrapper, int index)
        {
            // Scalar doubles are null when equal to the 64 bit floating point NaN.
            return !isNoValueDouble(bufferWrapper.getDouble(index));
        }
    }

    protected static class ScalarDataBuffer implements VPFDataBuffer
    {
        protected ScalarReader reader;
        protected ScalarAccessor accessor;
        protected BufferWrapper buffer;
        protected int position;

        public ScalarDataBuffer(ScalarReader reader, ScalarAccessor accessor, BufferFactory bufferFactory, int numRows)
        {
            this.reader = reader;
            this.accessor = accessor;
            this.buffer = bufferFactory.newBuffer(1 + numRows);
            // Start with position 1 that the coordinate N cooresponds to row id N.
            this.position = 1;
        }

        public Object get(int index)
        {
            return this.accessor.get(this.buffer, index);
        }

        public Object getBackingData()
        {
            return this.buffer;
        }

        public boolean hasValue(int index)
        {
            return this.accessor.hasValue(this.buffer, index);
        }

        public void read(ByteBuffer byteBuffer)
        {
            double d = this.reader.read(byteBuffer);
            this.buffer.putDouble(this.position, d);
            this.position++;
        }

        public void read(ByteBuffer byteBuffer, int length)
        {
            // Intentionally ignoring the length parameter. Numeric fields can never be variable length.
            this.read(byteBuffer);
        }
    }

    //**************************************************************//
    //********************  Vector Data  ***************************//
    //**************************************************************//

    protected interface VecReader
    {
        int getCoordsPerElem();

        VecBuffer read(ByteBuffer byteBuffer, int length);
    }

    protected abstract static class AbstractVecReader implements VecReader
    {
        protected int coordsPerElem;
        protected int bytesPerCoord;

        public AbstractVecReader(int coordsPerElem, int bytesPerCoord)
        {
            this.coordsPerElem = coordsPerElem;
            this.bytesPerCoord = bytesPerCoord;
        }

        public int getCoordsPerElem()
        {
            return this.coordsPerElem;
        }

        public VecBuffer read(ByteBuffer byteBuffer, int length)
        {
            VecBuffer vecBuffer = null;

            int prevLimit = byteBuffer.limit();
            int limit = byteBuffer.position() + (this.coordsPerElem * this.bytesPerCoord * length);
            try
            {
                byteBuffer.limit(limit);
                BufferWrapper newBuffer = this.doRead(byteBuffer);
                vecBuffer = new VecBuffer(this.coordsPerElem, newBuffer);
            }
            finally
            {
                byteBuffer.limit(prevLimit);
                byteBuffer.position(limit);
            }

            return vecBuffer;
        }

        protected abstract BufferWrapper doRead(ByteBuffer byteBuffer);
    }

    protected static class ShortVecReader extends AbstractVecReader
    {
        private short[] tmpBuffer;

        public ShortVecReader(int coordsPerElem)
        {
            super(coordsPerElem, (Short.SIZE / 8));
        }

        protected BufferWrapper doRead(ByteBuffer byteBuffer)
        {
            ShortBuffer shortBuffer = byteBuffer.asShortBuffer();

            // Replace null (NaN) values in partially null coordinates with 0. Because these vector coordinate buffers
            // are passed directly to GL, we avoid compatability problems with some graphics drivers by removing any
            // NaN coordinate in the vectors. We must also detect when a vector is completely null, and enter an empty
            // sub-buffer in this case. This is necessary because we are eliminating the NaN signal which the data
            // consumer would ordinarily use to detect null coordinates.
            if (this.replaceNaN(shortBuffer, (short) 0) <= 0)
                return null;

            return new BufferWrapper.ShortBufferWrapper(shortBuffer);
        }

        protected int replaceNaN(ShortBuffer shortBuffer, short value)
        {
            int length = shortBuffer.remaining();
            int numValues = 0;

            if (this.tmpBuffer == null || this.tmpBuffer.length < shortBuffer.remaining())
                this.tmpBuffer = new short[length];

            shortBuffer.get(this.tmpBuffer, 0, length);
            shortBuffer.flip();

            for (int i = 0; i < length; i++)
            {
                if (isNoValueShort(this.tmpBuffer[i]))
                    this.tmpBuffer[i] = value;
                else
                    numValues++;
            }

            shortBuffer.put(this.tmpBuffer, 0, length);
            shortBuffer.flip();

            return numValues;
        }
    }

    protected static class IntVecReader extends AbstractVecReader
    {
        private int[] tmpBuffer;

        public IntVecReader(int coordsPerElem)
        {
            super(coordsPerElem, (Integer.SIZE / 8));
        }

        protected BufferWrapper doRead(ByteBuffer byteBuffer)
        {
            IntBuffer intBuffer = byteBuffer.asIntBuffer();

            // Replace null (NaN) values in partially null coordinates with 0. Because these vector coordinate buffers
            // are passed directly to GL, we avoid compatability problems with some graphics drivers by removing any
            // NaN coordinate in the vectors. We must also detect when a vector is completely null, and enter an empty
            // sub-buffer in this case. This is necessary because we are eliminating the NaN signal which the data
            // consumer would ordinarily use to detect null coordinates.
            if (this.replaceNaN(intBuffer, 0) <= 0)
                return null;

            return new BufferWrapper.IntBufferWrapper(intBuffer);
        }

        protected int replaceNaN(IntBuffer intBuffer, int value)
        {
            int length = intBuffer.remaining();
            int numValues = 0;

            if (this.tmpBuffer == null || this.tmpBuffer.length < intBuffer.remaining())
                this.tmpBuffer = new int[length];

            intBuffer.get(this.tmpBuffer, 0, length);
            intBuffer.flip();

            for (int i = 0; i < length; i++)
            {
                if (isNoValueInt(this.tmpBuffer[i]))
                    this.tmpBuffer[i] = value;
                else
                    numValues++;
            }

            intBuffer.put(this.tmpBuffer, 0, length);
            intBuffer.flip();

            return numValues;
        }
    }

    protected static class FloatVecReader extends AbstractVecReader
    {
        private float[] tmpBuffer;

        public FloatVecReader(int coordsPerElem)
        {
            super(coordsPerElem, (Float.SIZE / 8));
        }

        protected BufferWrapper doRead(ByteBuffer byteBuffer)
        {
            FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();

            // Replace null (NaN) values in partially null coordinates with 0. Because these vector coordinate buffers
            // are passed directly to GL, we avoid compatability problems with some graphics drivers by removing any
            // NaN coordinate in the vectors. We must also detect when a vector is completely null, and enter an empty
            // sub-buffer in this case. This is necessary because we are eliminating the NaN signal which the data
            // consumer would ordinarily use to detect null coordinates.
            if (this.replaceNaN(floatBuffer, 0f) <= 0)
                return null;

            return new BufferWrapper.FloatBufferWrapper(floatBuffer);
        }

        protected int replaceNaN(FloatBuffer floatBuffer, float value)
        {
            int length = floatBuffer.remaining();
            int numValues = 0;

            if (this.tmpBuffer == null || this.tmpBuffer.length < floatBuffer.remaining())
                this.tmpBuffer = new float[length];

            floatBuffer.get(this.tmpBuffer, 0, length);
            floatBuffer.flip();

            for (int i = 0; i < length; i++)
            {
                if (isNoValueFloat(this.tmpBuffer[i]))
                    this.tmpBuffer[i] = value;
                else
                    numValues++;
            }

            floatBuffer.put(this.tmpBuffer, 0, length);
            floatBuffer.flip();

            return numValues;
        }
    }

    protected static class DoubleVecReader extends AbstractVecReader
    {
        private double[] tmpBuffer;

        public DoubleVecReader(int coordsPerElem)
        {
            super(coordsPerElem, Double.SIZE / 8);
        }

        protected BufferWrapper doRead(ByteBuffer byteBuffer)
        {
            DoubleBuffer doubleBuffer = byteBuffer.asDoubleBuffer();

            // Replace null (NaN) values in partially null coordinates with 0. Because these vector coordinate buffers
            // are passed directly to GL, we avoid compatability problems with some graphics drivers by removing any
            // NaN coordinate in the vectors. We must also detect when a vector is completely null, and enter an empty
            // sub-buffer in this case. This is necessary because we are eliminating the NaN signal which the data
            // consumer would ordinarily use to detect null coordinates.
            if (this.replaceNaN(doubleBuffer, 0d) <= 0)
                return null;

            return new BufferWrapper.DoubleBufferWrapper(doubleBuffer);
        }

        protected int replaceNaN(DoubleBuffer doubleBuffer, double value)
        {
            int length = doubleBuffer.remaining();
            int numValues = 0;

            if (this.tmpBuffer == null || this.tmpBuffer.length < doubleBuffer.remaining())
                this.tmpBuffer = new double[length];

            doubleBuffer.get(this.tmpBuffer, 0, length);
            doubleBuffer.flip();

            for (int i = 0; i < length; i++)
            {
                if (isNoValueDouble(this.tmpBuffer[i]))
                    this.tmpBuffer[i] = value;
                else
                    numValues++;
            }

            doubleBuffer.put(this.tmpBuffer, 0, length);
            doubleBuffer.flip();

            return numValues;
        }
    }

    protected static class VecDataBuffer implements VPFDataBuffer
    {
        protected VecReader reader;
        protected VecBufferSequence buffer;

        public VecDataBuffer(VecReader reader, int coordsPerElem, BufferFactory bufferFactory, int numRows,
            int elementsPerRow)
        {
            int bufferLength = Math.max(1, elementsPerRow);
            BufferWrapper buffer = bufferFactory.newBuffer((1 + numRows) * coordsPerElem * bufferLength);

            this.reader = reader;
            this.buffer = new VecBufferSequence(new VecBuffer(coordsPerElem, buffer), 1 + numRows);
            // Insert an empty coordinate so that the coordinate N cooresponds to row id N.
            this.buffer.append(VecBuffer.emptyVecBuffer(coordsPerElem));
        }

        public Object get(int index)
        {
            return this.buffer.subBuffer(index);
        }

        public Object getBackingData()
        {
            return this.buffer;
        }

        public boolean hasValue(int index)
        {
            // Variable length vector is null if it has zero length. Fixed length vector is null if all coordinates are
            // null, in which case the sub-buffer entry will also have zero length because we have detected this case
            // at read time. Early detection and handling of null coordinates is necessary because we replace null (NaN)
            // values in partially null coordinates with 0. Therefore at this stage a completely null coordinate, if
            // left as all 0 values, would be ambiguous from non-null values. 
            return this.buffer.subBufferSize(index) > 0;
        }

        public void read(ByteBuffer byteBuffer)
        {
            int length = byteBuffer.getInt();
            this.read(byteBuffer, length);
        }

        public void read(ByteBuffer byteBuffer, int length)
        {
            VecBuffer vecBuffer = this.reader.read(byteBuffer, length);

            if (vecBuffer != null)
            {
                this.buffer.append(vecBuffer);
            }
            else
            {
                this.buffer.append(VecBuffer.emptyVecBuffer(this.buffer.getCoordsPerVec()));
            }
        }
    }
}
