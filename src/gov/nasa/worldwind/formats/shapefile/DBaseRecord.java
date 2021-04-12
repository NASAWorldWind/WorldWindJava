/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */
package gov.nasa.worldwind.formats.shapefile;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.Logging;

import java.nio.*;
import java.text.*;
import java.util.logging.Level;

/**
 * @author Patrick Murris
 * @version $Id: DBaseRecord.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class DBaseRecord extends AVListImpl
{
    private boolean deleted = false;
    private int recordNumber;
    private static final DateFormat dateformat = new SimpleDateFormat("yyyyMMdd");

    public DBaseRecord(DBaseFile dbaseFile, ByteBuffer buffer, int recordNumber)
    {
        if (dbaseFile == null)
        {
            String message = Logging.getMessage("nullValue.DBaseFileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.readFromBuffer(dbaseFile, buffer, recordNumber);
    }

    public boolean isDeleted()
    {
        return this.deleted;
    }

    public int getRecordNumber()
    {
        return this.recordNumber;
    }

    @SuppressWarnings({"StringEquality"})
    protected void readFromBuffer(DBaseFile dbaseFile, ByteBuffer buffer, int recordNumber)
    {
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Set parent DBaseFile and record number.
        this.recordNumber = recordNumber;

        // Read deleted record flag.
        byte b = buffer.get();
        this.deleted = (b == 0x2A);

        // Create a buffer to hold the field values.
        int maxFieldLength = 0;
        for (DBaseField field : dbaseFile.getFields())
        {
            if (maxFieldLength < field.getLength())
                maxFieldLength = field.getLength();
        }

        // Read field values.
        DBaseField[] fields = dbaseFile.getFields();
        byte[] bytes = new byte[maxFieldLength];

        for (DBaseField field : fields)
        {
            int numRead = dbaseFile.readZeroTerminatedString(buffer, bytes, field.getLength());

            // Add a null entry for this field if the field's value is null or the empty string. This enables
            // applications to treat the DBaseRecord a standard AVList without any knowledge of the DBase file's field
            // keys. Specifically, DBaseRecord.hasKey() returns true for all fields.
            if (dbaseFile.isStringEmpty(bytes, numRead))
            {
                this.setValue(field.getName(), null);
                continue;
            }

            String value = dbaseFile.decodeString(bytes, numRead).trim();

            try
            {
                if (field.getType() == DBaseField.TYPE_BOOLEAN)
                {
                    this.setValue(field.getName(), value.equalsIgnoreCase("T") || value.equalsIgnoreCase("Y"));
                }
                else if (field.getType() == DBaseField.TYPE_CHAR)
                {
                    this.setValue(field.getName(), value);
                }
                else if (field.getType() == DBaseField.TYPE_DATE)
                {
                    this.setValue(field.getName(), dateformat.parse(value));
                }
                else if (field.getType() == DBaseField.TYPE_NUMBER)
                {
                    // Parse the field value as a decimal number. Double.parseDouble ignores any leading or trailing
                    // whitespace.
                    if (field.getDecimals() > 0)
                        this.setValue(field.getName(), Double.valueOf(value));
                    else
                        this.setValue(field.getName(), Long.valueOf(value));
                }
            }
            catch (Exception e)
            {
                // Log warning but keep reading.
                Logging.logger().log(Level.WARNING, Logging.getMessage("SHP.FieldParsingError", field, value), e);
            }
        }
    }
}
