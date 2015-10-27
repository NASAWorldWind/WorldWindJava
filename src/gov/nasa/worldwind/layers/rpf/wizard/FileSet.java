/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.rpf.wizard;

import gov.nasa.worldwind.util.wizard.WizardProperties;

import java.util.Collection;
import java.io.File;

/**
 * @author dcollins
 * @version $Id: FileSet.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class FileSet extends WizardProperties
{
    public static final String IDENTIFIER = "fileSet.Identifier";
    public static final String FILES = "fileSet.Files";
    public static final String TITLE = "fileSet.Title";
    public static final String SELECTED = "fileSet.Selected";

    public FileSet()
    {
    }

    public String getIdentifier()
    {
        return getStringProperty(IDENTIFIER);
    }

    public void setIdentifier(String identifier)
    {
        setProperty(IDENTIFIER, identifier);
    }

    @SuppressWarnings({"unchecked"})
    public Collection<File> getFiles()
    {
        Object value = getProperty(FILES);
        return (value != null && value instanceof Collection) ? (Collection<File>) value : null;
    }

    public void setFiles(Collection<File> files)
    {
        setProperty(FILES, files);
    }

    public int getFileCount()
    {
        Collection<File> files = getFiles();
        return files != null ? files.size() : 0;
    }

    public String getTitle()
    {
        return getStringProperty(TITLE);
    }

    public void setTitle(String title)
    {
        setProperty(TITLE, title);
    }

    public boolean isSelected()
    {
        Boolean b = getBooleanProperty(SELECTED);
        return b != null ? b : false;
    }

    public void setSelected(boolean b)
    {
        setProperty(SELECTED, b);
    }
}
