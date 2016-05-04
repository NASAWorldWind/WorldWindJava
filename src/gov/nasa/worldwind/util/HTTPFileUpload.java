/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

/**
 * @author Lado Garakanidze
 * @version $Id: HTTPFileUpload.java 1171 2013-02-11 21:45:02Z dcollins $
 */

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.WWRuntimeException;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

/** Synchronous file upload using HTTP POST as a multi-part form data */
public class HTTPFileUpload extends java.util.Observable
{
    protected static final String CR_LF = "\r\n";
    protected static final String TWO_HYPHENS = "--";
    protected static final String BOUNDARY = "*********NASA_World_Wind_HTTP_File_Upload_Separator**********";
    protected int maxBufferSize = 1024 * 1024; // default is 1M

    protected final URL url;

    protected ArrayList<FileInfo> filesToUpload = new ArrayList<FileInfo>();

    protected String requestMethod = "POST";
    protected AVList requestProperties = new AVListImpl();

    protected long totalBytesToUpload = (long) 0;
    protected long totalBytesUploaded = (long) 0;
    protected int totalFilesUploaded = 0;
    protected int totalFilesFailed = 0;

    protected class FileInfo
    {
        protected final String uploadName;
        protected final Object uploadItem;
        protected final AVList properties;

        public FileInfo(String name, Object item, AVList properties)
        {
            this.uploadName = name;
            this.uploadItem = item;
            this.properties = properties;
        }
    }

    public HTTPFileUpload(URL url)
    {
        if (url == null)
        {
            throw new IllegalArgumentException();
        }

        this.url = url;

        this.setRequestMethod("POST");
        this.setRequestProperty("Connection", "Keep-Alive");
        this.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        this.setRequestProperty("Content-Transfer-Encoding", "binary");
    }

    public long getTotalFilesToUpload()
    {
        return filesToUpload.size();
    }

    public long getTotalBytesToUpload()
    {
        return totalBytesToUpload;
    }

    public long getTotalBytesUploaded()
    {
        return totalBytesUploaded;
    }

    public int getTotalFilesUploaded()
    {
        return totalFilesUploaded;
    }

    public int getTotalFilesFailed()
    {
        return totalFilesFailed;
    }

    public int getMaxBufferSize()
    {
        return maxBufferSize;
    }

    public void setMaxBufferSize(int maxBufferSize)
    {
        this.maxBufferSize = maxBufferSize;
    }

    /**
     * Sets a HTTP request method - POST or GET
     *
     * @param method POST or GET
     */
    public void setRequestMethod(String method)
    {
        if ("POST".equalsIgnoreCase(method))
            this.requestMethod = "POST";
        else if ("GET".equalsIgnoreCase(method))
            this.requestMethod = "GET";
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns the HTTP request method - POST or GET
     *
     * @return POST or GET
     */
    public String getRequestMethod()
    {
        return this.requestMethod;
    }

    public void setRequestProperty(String name, String value)
    {
        if (WWUtil.isEmpty(name))
        {
            throw new IllegalArgumentException();
        }
        this.requestProperties.setValue(name, value);
    }

//    public void add(String stringToUpload, String name)
//    {
//        if (stringToUpload == null)
//        {
//            throw new IllegalArgumentException("String to upload is null");
//        }
//
//        if (name == null)
//            throw new IllegalArgumentException("String name to upload is null");
//
//        if (stringToUpload.length() == 0)
//            throw new IllegalArgumentException("String to upload is empty");
//
//        this.totalBytesToUpload += stringToUpload.length();
//        this.filesToUpload.add(new FileInfo(name, stringToUpload, null));
//    }

    public void add(ByteBuffer bufferToUpload, String name, AVList params)
    {
        if (bufferToUpload == null)
        {
            throw new IllegalArgumentException();
        }

        if (WWUtil.isEmpty(name))
        {
            throw new IllegalArgumentException();
        }

        if (bufferToUpload.limit() == 0)
        {
            throw new IllegalArgumentException();
        }

        this.totalBytesToUpload += bufferToUpload.limit();
        this.filesToUpload.add(new FileInfo(name, bufferToUpload, params));
    }

    /**
     * Adds a file to the HTTP File Uploader.
     *
     * @param file   The file to upload, must exist
     * @param name   The desired name of the file
     * @param params AVList of parameters
     *
     * @throws FileNotFoundException if the file was not found or does not exist
     */
    public void add(File file, String name, AVList params) throws FileNotFoundException
    {
        if (null != file && file.exists())
        {
            this.totalBytesToUpload += file.length();
            this.filesToUpload.add(new FileInfo(name, file, params));
        }
        else
            throw new FileNotFoundException((file != null) ? file.getName() : "");
    }

    public void send() throws Exception
    {
        for (FileInfo info : this.filesToUpload)
        {
            try
            {
                if (info.uploadItem instanceof File)
                    send((File) info.uploadItem, info.uploadName, info.properties);
                else if (info.uploadItem instanceof ByteBuffer)
                    send((ByteBuffer) info.uploadItem, info.uploadName, info.properties);
                else if (info.uploadItem instanceof String)
                    send((String) info.uploadItem, info.uploadName, info.properties);

                this.totalFilesUploaded++;
            }
            catch (Exception e)
            {
                this.totalFilesFailed++;

                String reason = WWUtil.extractExceptionReason(e);

                throw new WWRuntimeException();
            }
        }
    }

    protected void send(File fileToUpload, String uploadName, AVList params)
        throws IOException, NullPointerException
    {
        if (null == fileToUpload || !fileToUpload.exists())
            throw new FileNotFoundException();

        if (null == url)
        {
            throw new IllegalArgumentException();
        }

        HttpURLConnection conn = null;
        FileInputStream fis = null;
        DataOutputStream dos = null;

        int bytesRead, bytesAvailable, bufferSize;

        try
        {
            conn = (HttpURLConnection) this.url.openConnection();
            conn.setDoInput(true);  // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false);   // Don't use a cached copy.
            this.writeRequestProperties(conn);

            dos = new DataOutputStream(conn.getOutputStream());

            this.writeProperties(dos, params);

            this.writeContentDisposition(dos, uploadName);

            // create a buffer of maximum size
            fis = new FileInputStream(fileToUpload);
            bytesAvailable = fis.available();

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = ByteBuffer.allocate(bufferSize).array();

            // read file and write it into form...
            bytesRead = fis.read(buffer, 0, bufferSize);
            while (bytesRead > 0)
            {
                dos.write(buffer, 0, bytesRead);

                this.totalBytesUploaded += (long) bytesRead;
                this.notifyProgress();

                bytesAvailable = fis.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fis.read(buffer, 0, bufferSize);
            }

            this.writeContentSeparator(dos);
            dos.flush();

            this.handleResponse(conn);
        }
        finally
        {
            WWIO.closeStream(fis, null);
            WWIO.closeStream(dos, null);
            this.disconnect(conn);
        }
    }

    protected void handleResponse(HttpURLConnection conn) throws IOException
    {
        if (null != conn)
        {
            int code = conn.getResponseCode();
            String message = conn.getResponseMessage();

            if (code != 200)
            {
                String reason = "(" + code + ") :" + message;
                throw new IOException(reason);
            }
        }
        else
        {
            throw new IOException((String) null);
        }
    }

    protected void disconnect(HttpURLConnection conn)
    {
        if (null != conn)
        {
            try
            {
                conn.disconnect();
            }
            catch (Exception e)
            {
                }
        }
    }

    protected void send(ByteBuffer bufferToUpload, String fileName, AVList params) throws IOException
    {
        if (null == bufferToUpload)
        {
            throw new IllegalArgumentException();
        }

        if (bufferToUpload.limit() == 0)
        {
            throw new IllegalArgumentException();
        }

        if (null == url)
        {
            throw new IllegalArgumentException();
        }

        if (WWUtil.isEmpty(fileName))
        {
            throw new IllegalArgumentException();
        }

        HttpURLConnection conn = null;
        DataOutputStream dos = null;

        try
        {
            conn = (HttpURLConnection) this.url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);   // Don't use a cached copy.
            this.writeRequestProperties(conn);

            dos = new DataOutputStream(conn.getOutputStream());

            this.writeProperties(dos, params);

            this.writeContentDisposition(dos, fileName);

            int bytesAvailable = bufferToUpload.rewind().remaining();

            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = ByteBuffer.allocate(bufferSize).array();

            // Send buffer to server
            bufferToUpload.rewind();
            while (bufferToUpload.hasRemaining())
            {
                int bytesToRead = Math.min(bufferToUpload.remaining(), maxBufferSize);
                bufferToUpload.get(buffer, 0, bytesToRead);
                dos.write(buffer, 0, bytesToRead);

                this.totalBytesUploaded += (long) bytesToRead;
                this.notifyProgress();
            }

            this.writeContentSeparator(dos);
            dos.flush();

            this.handleResponse(conn);
        }
        finally
        {
            WWIO.closeStream(dos, null);
            this.disconnect(conn);
        }
    }

    protected void send(String stringToUpload, String fileName, AVList params) throws IOException
    {
        if (WWUtil.isEmpty(stringToUpload))
        {
            throw new IllegalArgumentException();
        }

        if (null == url)
        {
            throw new IllegalArgumentException();
        }

        if (WWUtil.isEmpty(fileName))
        {
            throw new IllegalArgumentException();
        }

        HttpURLConnection conn = null;
        DataOutputStream dos = null;

        try
        {
            conn = (HttpURLConnection) this.url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);   // Don't use a cached copy.
            this.writeRequestProperties(conn);

            dos = new DataOutputStream(conn.getOutputStream());

            this.writeProperties(dos, params);

            this.writeContentDisposition(dos, fileName);

            byte[] buffer = stringToUpload.getBytes("UTF-8");
            dos.write(buffer, 0, buffer.length);
            this.totalBytesUploaded += (long) stringToUpload.length();
            this.notifyProgress();

            this.writeContentSeparator(dos);
            dos.flush();

            this.handleResponse(conn);
        }
        finally
        {
            WWIO.closeStream(dos, null);
            this.disconnect(conn);
        }
    }

    protected void writeProperties(DataOutputStream dos, AVList params) throws IOException
    {
        if (null != dos && null != params)
        {
            for (Map.Entry<String, Object> param : params.getEntries())
            {
                String name = param.getKey();
                String value = AVListImpl.getStringValue(params, name, "");
                this.writeContentDisposition(dos, name, value);
            }
        }
    }

    /**
     * Writes HTTP request' properties (HTTP headers)
     *
     * @param conn HttpURLConnection connection
     *
     * @throws IOException if there is any problem with a connection
     */
    protected void writeRequestProperties(HttpURLConnection conn) throws IOException
    {
        if (null != conn)
        {
            conn.setRequestMethod(this.getRequestMethod());
            for (Map.Entry<String, Object> requestProperty : this.requestProperties.getEntries())
            {
                conn.setRequestProperty(requestProperty.getKey(), (String) requestProperty.getValue());
            }
        }
    }

    protected void writeContentDisposition(DataOutputStream dos, String filename) throws IOException
    {
        if (null != dos)
        {
            dos.writeBytes(TWO_HYPHENS + BOUNDARY + CR_LF);
            dos.writeBytes("Content-Disposition: attachment; filename=\"" + filename + "\"" + CR_LF);
            dos.writeBytes("Content-type: application/octet-stream" + CR_LF);
            dos.writeBytes(CR_LF);
        }
    }

    protected void writeContentDisposition(DataOutputStream dos, String paramName, String paramValue) throws IOException
    {
        if (null != dos && null != paramName)
        {
            dos.writeBytes(TWO_HYPHENS + BOUNDARY + CR_LF);
            dos.writeBytes("Content-Disposition: form-data; name=\"" + paramName + "\"" + CR_LF);
            dos.writeBytes(CR_LF + paramValue + CR_LF);
        }
    }

    protected void writeContentSeparator(DataOutputStream dos) throws IOException
    {
        if (null != dos)
        {
            // send multipart form data necesssary after file data...
            dos.writeBytes(CR_LF + TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + CR_LF);
        }
    }

    protected void notifyProgress()
    {
        double progress = (double) 100 * (double) this.totalBytesUploaded / (double) this.totalBytesToUpload;

        this.setChanged();
        this.notifyObservers(new Float(progress));
    }
}