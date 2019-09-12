/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * @author dcollins
 * @version $Id: VPFBufferedRecordData.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFBufferedRecordData implements Iterable<VPFRecord>
{
    protected static class RecordData
    {
        public VPFDataBuffer dataBuffer;
        protected Map<Object, Integer> recordIndex;

        public RecordData(VPFDataBuffer dataBuffer)
        {
            this.dataBuffer = dataBuffer;
        }

        public boolean hasIndex()
        {
            return this.recordIndex != null;
        }

        public int indexOf(Object value, int startIndex, int endIndex)
        {
            int index = -1;

            if (this.recordIndex != null)
            {
                Integer i = this.recordIndex.get(value);
                if (i != null)
                    index = i;
            }
            else
            {
                for (int i = startIndex; i <= endIndex; i++)
                {
                    Object o = this.dataBuffer.get(i);
                    if ((o != null) ? o.equals(value) : (value == null))
                    {
                        index = i;
                        break;
                    }
                }
            }

            return index;
        }

        public boolean updateIndex(int startIndex, int endIndex)
        {
            if (this.recordIndex == null)
                this.recordIndex = new HashMap<Object, Integer>();

            this.recordIndex.clear();

            for (int index = startIndex; index <= endIndex; index++)
            {
                Object o = this.dataBuffer.get(index);
                this.recordIndex.put(o, index);
            }

            return true;
        }
    }

    private int numRecords;
    private Map<String, RecordData> dataMap = new HashMap<String, RecordData>();

    public VPFBufferedRecordData()
    {
    }

    public int getNumRecords()
    {
        return this.numRecords;
    }

    public void setNumRecords(int numRecords)
    {
        this.numRecords = numRecords;
    }

    public Iterable<String> getRecordParameterNames()
    {
        return Collections.unmodifiableSet(this.dataMap.keySet());
    }

    public VPFDataBuffer getRecordData(String parameterName)
    {
        if (parameterName == null)
        {
            String message = Logging.getMessage("nullValue.ParameterNameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RecordData data = this.dataMap.get(parameterName);
        return (data != null) ? data.dataBuffer : null;
    }

    public void setRecordData(String parameterName, VPFDataBuffer dataBuffer)
    {
        if (parameterName == null)
        {
            String message = Logging.getMessage("nullValue.ParameterNameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (dataBuffer != null)
        {
            this.dataMap.put(parameterName, new RecordData(dataBuffer));
        }
        else
        {
            this.dataMap.remove(parameterName);
        }
    }

    public VPFRecord getRecord(int id)
    {
        if (id < 1 || id > this.numRecords)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", id);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return new RecordImpl(id);
    }

    public VPFRecord getRecord(String parameterName, Object value)
    {
        if (parameterName == null)
        {
            String message = Logging.getMessage("nullValue.ParameterNameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RecordData data = this.dataMap.get(parameterName);
        if (data == null)
        {
            return null;
        }

        int index = data.indexOf(value, 1, this.numRecords);
        return (index > 0) ? new RecordImpl(index) : null;
    }

    public Iterator<VPFRecord> iterator()
    {
        return new Iterator<VPFRecord>()
        {
            private int id = 0;
            private int maxId = numRecords;

            public boolean hasNext()
            {
                return this.id < this.maxId;
            }

            public VPFRecord next()
            {
                return new RecordImpl(++this.id);
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    public boolean buildRecordIndex(String parameterName)
    {
        if (parameterName == null)
        {
            String message = Logging.getMessage("nullValue.ParameterNameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RecordData data = this.dataMap.get(parameterName);
        return (data != null) && data.updateIndex(1, this.numRecords);
    }

    public static int indexFromId(int rowId)
    {
        return rowId - 1;
    }

    //**************************************************************//
    //********************  Record Implementation  *****************//
    //**************************************************************//

    protected class RecordImpl implements VPFRecord
    {
        protected final int id;

        public RecordImpl(int id)
        {
            this.id = id;
        }

        public int getId()
        {
            return this.id;
        }

        public boolean hasValue(String parameterName)
        {
            VPFDataBuffer dataBuffer = getRecordData(parameterName);
            return (dataBuffer != null) && dataBuffer.hasValue(this.id);
        }

        public Object getValue(String parameterName)
        {
            VPFDataBuffer dataBuffer = getRecordData(parameterName);
            return (dataBuffer != null) ? dataBuffer.get(this.id) : null;
        }
    }
}
