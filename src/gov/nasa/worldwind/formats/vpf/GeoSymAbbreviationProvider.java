/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.util.*;

import java.io.InputStream;
import java.util.*;

/**
 * @author Patrick Murris
 * @version $Id: GeoSymAbbreviationProvider.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeoSymAbbreviationProvider
{
    private HashMap<Integer, HashMap<Integer, String>> abbreviationTables;

    public GeoSymAbbreviationProvider(String filePath)
    {
        this.initialize(filePath);
    }

    public String getAbbreviation(int tableId, int abbreviationId)
    {
        HashMap<Integer, String> table = this.abbreviationTables.get(tableId);
        if (table == null)
            return null;

        return table.get(abbreviationId);
    }

    protected void initialize(String filePath)
    {
        InputStream inputStream = WWIO.openFileOrResourceStream(filePath, this.getClass());
        if (inputStream == null)
        {
            String message = Logging.getMessage("generic.ExceptionWhileReading", filePath);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        readTables(new Scanner(inputStream));
    }

    protected void readTables(Scanner scanner)
    {
        this.abbreviationTables = new HashMap<Integer, HashMap<Integer, String>>();

        HashMap<Integer, String> table = null;
        String s;

        // Skip header
        while (!(scanner.nextLine()).equals(";"))
        {
        }

        // Read tables
        while (scanner.hasNextLine())
        {
            s = scanner.nextLine().trim();
            if (s.length() == 0)
                continue;

            if (s.endsWith(":"))
            {
                // Table ID
                Integer id = Integer.parseInt(s.split(":")[0]);
                table = new HashMap<Integer, String>();
                this.abbreviationTables.put(id, table);
            }
            else
            {
                // Table record
                String[] tokens = s.split("[|]");
                Integer id = Integer.parseInt(tokens[0]);
                table.put(id, tokens[1]);
            }
        }
    }
}
