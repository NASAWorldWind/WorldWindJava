/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render.airspaces;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.globes.*;

import javax.media.opengl.*;
import java.nio.*;
import java.util.Arrays;

/**
 * @author dcollins
 * @version $Id: Geometry.java 2210 2014-08-08 22:06:02Z tgaskins $
 */
public class Geometry extends AVListImpl implements Cacheable
{
    public static class CacheKey
    {
        private final GlobeStateKey globeStateKey;
        private final Class cls;
        private final String key;
        private final Object[] params;
        private int hash = 0;

        public CacheKey(Globe globe, Class cls, String key, Object... params)
        {
            this.globeStateKey = globe != null ? globe.getGlobeStateKey() : null;
            this.cls = cls;
            this.key = key;
            this.params = params;
        }

        public CacheKey(Class cls, String key, Object... params)
        {
            this(null, cls, key, params);
        }

        public CacheKey(String key, Object... params)
        {
            this(null, null, key, params);
        }

        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || this.getClass() != o.getClass())
                return false;

            CacheKey that = (CacheKey) o;

            if (this.globeStateKey != null ? !this.globeStateKey.equals(that.globeStateKey) : that.globeStateKey != null)
                return false;
            if (this.cls != null ? !this.cls.equals(that.cls) : that.cls != null)
                return false;
            if (this.key != null ? !this.key.equals(that.key) : that.key != null)
                return false;
            //noinspection RedundantIfStatement
            if (!Arrays.deepEquals(this.params, that.params))
                return false;

            return true;
        }

        public int hashCode()
        {
            if (this.hash == 0)
            {
                int result;
                result = (this.globeStateKey != null ? this.globeStateKey.hashCode() : 0);
                result = 31 * result + (this.cls != null ? this.cls.hashCode() : 0);
                result = 31 * result + (this.key != null ? this.key.hashCode() : 0);
                result = 31 * result + (this.params != null ? Arrays.deepHashCode(this.params) : 0);
                this.hash = result;
            }

            return this.hash;
        }
    }

    public static final int TEXTURE = 0;
    public static final int ELEMENT = 1;
    public static final int VERTEX = 2;
    public static final int NORMAL = 3;

    private int[] mode;
    private int[] count;
    private int[] size;
    private int[] glType;
    private int[] stride;
    private Buffer[] buffer;

    public Geometry()
    {
        this.mode = new int[4];
        this.count = new int[4];
        this.size = new int[4];
        this.glType = new int[4];
        this.stride = new int[4];
        this.buffer = new Buffer[4];
    }

    public int getMode(int object)
    {
        return this.mode[object];
    }

    public void setMode(int type, int mode)
    {
        this.mode[type] = mode;
    }

    public int getCount(int type)
    {
        return this.count[type];
    }

    public int getSize(int type)
    {
        return this.size[type];
    }

    public int getGLType(int type)
    {
        return this.glType[type];
    }

    public int getStride(int type)
    {
        return this.stride[type];
    }

    public Buffer getBuffer(int type)
    {
        return this.buffer[type];
    }

    public void setData(int type, int size, int glType, int stride, int count, int[] src, int srcPos)
    {
        this.size[type] = size;
        this.glType[type] = glType;
        this.stride[type] = stride;
        this.count[type] = count;

        int numCoords = size * count;
        if (this.buffer[type] == null
            || this.buffer[type].capacity() < numCoords
            || !(this.buffer[type] instanceof IntBuffer))
        {
            this.buffer[type] = Buffers.newDirectIntBuffer(numCoords);
        }

        this.bufferCopy(src, srcPos, (IntBuffer) this.buffer[type], 0, numCoords);
    }

    public void setData(int type, int size, int stride, int count, float[] src, int srcPos)
    {
        this.size[type] = size;
        this.glType[type] = GL.GL_FLOAT;
        this.stride[type] = stride;
        this.count[type] = count;

        int numCoords = size * count;
        if (this.buffer[type] == null
            || this.buffer[type].capacity() < numCoords
            || !(this.buffer[type] instanceof FloatBuffer))
        {
            this.buffer[type] = Buffers.newDirectFloatBuffer(numCoords);
        }

        this.bufferCopy(src, srcPos, (FloatBuffer) this.buffer[type], 0, numCoords);
    }

    // version using float buffer instead of array
    public void setData(int type, int size, int stride, int count, FloatBuffer src)
    {
        this.size[type] = size;
        this.glType[type] = GL.GL_FLOAT;
        this.stride[type] = stride;
        this.count[type] = count;

        int numCoords = size * count;
        if (this.buffer[type] == null
            || this.buffer[type].capacity() < numCoords
            || !(this.buffer[type] instanceof FloatBuffer))
        {
            this.buffer[type] = src;
        }
    }

    public void setElementData(int mode, int count, int[] src)
    {
        this.setMode(ELEMENT, mode);
        this.setData(ELEMENT, 1, GL.GL_UNSIGNED_INT, 0, count, src, 0);
    }

    // version using buffer instead of array
    public void setElementData(int mode, int count, IntBuffer src)
    {
        this.setMode(ELEMENT, mode);
        this.buffer[ELEMENT] = src;
        this.size[ELEMENT] = 1;
        this.glType[ELEMENT] = GL.GL_UNSIGNED_INT;
        this.stride[ELEMENT] = 0;
        this.count[ELEMENT] = count;
    }

    public void setVertexData(int count, float[] src)
    {
        this.setData(VERTEX, 3, 0, count, src, 0);
    }

    // version using float buffer
    public void setVertexData(int count, FloatBuffer src)
    {
        this.buffer[VERTEX] = src;
        this.size[VERTEX] = 3;
        this.glType[VERTEX] = GL.GL_FLOAT;
        this.stride[VERTEX] = 0;
        this.count[VERTEX] = count;
    }

    public void setNormalData(int count, float[] src)
    {
        this.setData(NORMAL, 3, 0, count, src, 0);
    }

    // version using float buffer
    public void setNormalData(int count, FloatBuffer src)
    {
        this.buffer[NORMAL] = src;
        this.size[NORMAL] = 3;
        this.glType[NORMAL] = GL.GL_FLOAT;
        this.stride[NORMAL] = 0;
        this.count[NORMAL] = count;
    }

    public void setTextureCoordData(int count, float[] src)
    {
        this.setData(TEXTURE, 2, 0, count, src, 0);
    }

    // version using float buffer
    public void setTextureCoordData(int count, FloatBuffer src)
    {
        this.buffer[TEXTURE] = src;
        this.size[NORMAL] = 2;
        this.glType[NORMAL] = GL.GL_FLOAT;
        this.stride[NORMAL] = 0;
        this.count[NORMAL] = count;
    }

    public void clear(int type)
    {
        this.mode[type] = 0;
        this.count[type] = 0;
        this.size[type] = 0;
        this.glType[type] = 0;
        this.stride[type] = 0;
        this.buffer[type] = null;
    }

    public long getSizeInBytes()
    {
        return this.bufferSize(ELEMENT) + this.bufferSize(VERTEX) + this.bufferSize(NORMAL);
    }

    private long bufferSize(int bufferType)
    {
        long size = 0L;
        if (this.buffer[bufferType] != null)
            size = this.sizeOf(this.glType[bufferType]) * this.buffer[bufferType].capacity();
        return size;
    }

    private long sizeOf(int glType)
    {
        long size = 0L;
        switch (glType)
        {
            case GL2.GL_BYTE:
                size = 1L;
                break;
            case GL2.GL_SHORT:
            case GL2.GL_UNSIGNED_SHORT:
                size = 2L;
                break;
            case GL2.GL_INT:
            case GL2.GL_UNSIGNED_INT:
            case GL2.GL_FLOAT:
                size = 4L;
                break;
            case GL2.GL_DOUBLE:
                size = 8L;
                break;
        }
        return size;
    }

    private void bufferCopy(int[] src, int srcPos, IntBuffer dest, int destPos, int length)
    {
        dest.position(destPos);
        dest.put(src, srcPos, length);
        dest.position(destPos);
    }

    private void bufferCopy(float[] src, int srcPos, FloatBuffer dest, int destPos, int length)
    {
        dest.position(destPos);
        dest.put(src, srcPos, length);
        dest.position(destPos);
    }
}
