/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.cache;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.retrieve.*;
import gov.nasa.worldwind.util.*;

import java.beans.PropertyChangeEvent;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Basic implementation of {@link FileStore}.
 *
 * @author Tom Gaskins
 * @version $Id: BasicDataFileStore.java 1950 2014-04-20 18:52:47Z tgaskins $
 */
public class BasicDataFileStore extends AbstractFileStore
{
    /** The number of milliseconds to wait before a retrieval request for the same file can be reissued. */
    protected static final long TIMEOUT = (long) 5e3;
    /** The default content types used to determine an unknown file format in <code>requestFile</code>. */
    protected static final List<String> DEFAULT_CACHE_CONTENT_TYPES = Arrays.asList(
        "application/vnd.google-earth.kml+xml",
        "application/vnd.google-earth.kmz",
        "model/collada+xml",
        "image/dds",
        "image/gif",
        "image/jpeg",
        "image/jpg",
        "image/png"
    );

    /** The map of cached entries. */
    protected BasicMemoryCache db = new BasicMemoryCache((long) 3e5, (long) 5e5);
    /**
     * Absent-resource list to keep track of resources that were requested by requestFile but failed. The default list
     * holds a maximum of 2000 entries, allows 3 attempts separated by 500 milliseconds before marking a resource
     * semi-permanently absent, and allows additional attempts after 60 seconds. The {@link #getAbsentResourceList()}
     * method may be overridden by subclasses if they wish to provide an alternatively configured absent-resource list.
     */
    protected AbsentResourceList absentResources = new AbsentResourceList(2000, 3, 500, 60000);
    /**
     * The list of content types used to determine an unknown file format in <code>requestFile</code>. If a URL is
     * requested that does not have a format suffix, <code>requestFile</code> appends a suffix appropriate for the
     * content type returned by the server. Subsequent calls to <code>requestFile</code> use the content types in this
     * list to find the content type matching the cached file.
     * <p>
     * This is initialized to the following list of default content types typically used in WorldWind applications:
     * <ul> <li>application/vnd.google-earth.kml+xml</li> <li>application/vnd.google-earth.kmz</li>
     * <li>model/collada+xml</li> <li>image/dds</li> <li>image/gif</li> <li>image/jpeg</li> <li>image/jpg</li>
     * <li>image/png</li> </ul>
     * <p>
     * This list may be overridden by specifying a comma-delimited list of content types in the WorldWind configuration
     * parameter <code>gov.nasa.worldwind.avkey.CacheContentTypes</code>.
     */
    protected List<String> cacheContentTypes = new ArrayList<String>(DEFAULT_CACHE_CONTENT_TYPES);

    /**
     * Create an instance.
     *
     * @throws IllegalStateException if the configuration file name cannot be determined from {@link Configuration} or
     *                               the configuration file cannot be found.
     */
    public BasicDataFileStore()
    {
        String configPath = Configuration.getStringValue(AVKey.DATA_FILE_STORE_CONFIGURATION_FILE_NAME);
        if (configPath == null)
        {
            String message = Logging.getMessage("FileStore.NoConfiguration");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        java.io.InputStream is = null;
        File configFile = new File(configPath);
        if (configFile.exists())
        {
            try
            {
                is = new FileInputStream(configFile);
            }
            catch (FileNotFoundException e)
            {
                String message = Logging.getMessage("FileStore.LocalConfigFileNotFound", configPath);
                Logging.logger().finest(message);
            }
        }

        if (is == null)
        {
            is = this.getClass().getClassLoader().getResourceAsStream(configPath);
        }

        if (is == null)
        {
            String message = Logging.getMessage("FileStore.ConfigurationNotFound", configPath);
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        this.initialize(is);
    }

    /**
     * Create an instance to manage a specified directory.
     *
     * @param directoryPath the directory to manage as a file store.
     */
    public BasicDataFileStore(File directoryPath)
    {
        if (directoryPath == null)
        {
            String message = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\"?>");

        sb.append("<dataFileStore><writeLocations><location wwDir=\"");
        sb.append(directoryPath.getAbsolutePath());
        sb.append("\" create=\"true\"/></writeLocations></dataFileStore>");

        this.initialize(WWIO.getInputStreamFromString(sb.toString()));
    }

    @Override
    protected void initialize(InputStream xmlConfigStream)
    {
        super.initialize(xmlConfigStream);

        String s = Configuration.getStringValue(AVKey.CACHE_CONTENT_TYPES);
        if (s != null)
        {
            this.cacheContentTypes.clear();

            String[] contentTypes = s.split(",");
            for (String type : contentTypes)
            {
                type = type.trim();
                if (!WWUtil.isEmpty(type))
                    this.cacheContentTypes.add(type);
            }
        }
    }

    /**
     * Returns this file store's absent-resource list.
     *
     * @return the file store's absent-resource list.
     */
    protected AbsentResourceList getAbsentResourceList()
    {
        return this.absentResources;
    }

    /**
     * Returns this file store's list of content types.
     *
     * @return the file store's list of content types.
     */
    protected List<String> getCacheContentTypes()
    {
        return this.cacheContentTypes;
    }

    public String getContentType(String address)
    {
        if (address == null)
            return null;

        DBEntry entry = (DBEntry) this.db.getObject(address);
        return entry != null ? entry.contentType : null;
    }

    public long getExpirationTime(String address)
    {
        if (address == null)
            return 0;

        DBEntry entry = (DBEntry) this.db.getObject(address);
        return entry != null ? entry.expiration : 0;
    }

    /** Holds information for entries in the cache database. */
    protected static class DBEntry implements Cacheable
    {
        protected final static int NONE = 0;
        protected final static int PENDING = 1;
        protected final static int LOCAL = 2;

        protected String name;
        protected String contentType;
        protected long expiration;
        protected URL localUrl;
        protected long lastUpdateTime;
        protected int state;

        public DBEntry(String name)
        {
            this.name = name;
            this.state = NONE;
            this.lastUpdateTime = System.currentTimeMillis();
        }

        public long getSizeInBytes()
        {
            return 40 + (name != null ? 2 * name.length() : 0);
        }
    }

    /** {@inheritDoc} */
    public synchronized void removeFile(String address)
    {
        if (address == null)
        {
            String message = Logging.getMessage("nullValue.AddressIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        DBEntry entry = (DBEntry) this.db.getObject(address);
        if (entry == null)
            return; // Nothing to delete

        // Delete the cache file
        this.removeFile(entry.localUrl);

        // Remove the entry from the database
        this.db.remove(address);
    }

    /** {@inheritDoc} */
    public synchronized URL requestFile(String address)
    {
        if (address == null)
        {
            String message = Logging.getMessage("nullValue.AddressIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        // Store remote files in the WorldWind cache by default. This provides backward compatibility with applications
        // depending on requestFile's behavior prior to the addition of the cacheRemoteFile parameter.
        return this.requestFile(address, true);
    }

    /** {@inheritDoc} */
    public synchronized URL requestFile(String address, boolean cacheRemoteFile)
    {
        if (address == null)
        {
            String message = Logging.getMessage("nullValue.AddressIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (this.getAbsentResourceList().isResourceAbsent(address))
            return null;

        DBEntry entry = (DBEntry) this.db.getObject(address);
        if (entry != null)
        {
            long now = System.currentTimeMillis();
            boolean expired = entry.expiration != 0 && now > entry.expiration;

            // Return the resource if it is local and has not expired.
            if (entry.state == DBEntry.LOCAL && !expired)
                return entry.localUrl;

            if (entry.state == DBEntry.PENDING && (now - entry.lastUpdateTime <= TIMEOUT))
                return null;
        }

        URL url = WWIO.makeURL(address); // this may or may not make a URL, depending on address type
        URL localUrl;

        // If the address is already a URL in the "file" scheme, we can just use return this URL. Otherwise,
        // attempt to find a local file for the address.
        if (url != null && "file".equalsIgnoreCase(url.getProtocol()))
            localUrl = url;
        else
            localUrl = this.getLocalFileUrl(address, url, cacheRemoteFile); // Don't look for temp files in the cache.

        if (localUrl != null) // file exists if local URL is non-null
            return localUrl;

        // If the address' URL is not null but the file was not found locally, try to make it local. Store the retrieved
        // file in the cache if cacheRemoteFile is true, otherwise store it in a temporary location.
        if (url != null && !this.getAbsentResourceList().isResourceAbsent(address))
            this.makeLocal(address, url, cacheRemoteFile);
        else if (url == null)
            this.getAbsentResourceList().markResourceAbsent(address); // no URL for address and not a local file

        return null;
    }

    /**
     * Returns a file from the cache, the local file system or the classpath if the file exists. The specified address
     * may be a jar URL. See {@link java.net.JarURLConnection} for a description of jar URLs. If
     * <code>searchLocalCache</code> is <code>true</code> this looks for the file in the WorldWind cache, otherwise
     * this only looks for the file in the local file system and the classpath.
     *
     * @param address          the name used to identify the cached file.
     * @param retrievalUrl     the URL to obtain the file if it is not in the cache. Used only to determine a location
     *                         to search in the local cache. May be null.
     * @param searchLocalCache <code>true</code> to look for the file in the WorldWind cache, otherwise
     *                         <code>false</code>.
     *
     * @return the requested file if it exists, otherwise null.
     *
     * @throws IllegalArgumentException if the specified address is null.
     */
    protected synchronized URL getLocalFileUrl(String address, URL retrievalUrl, boolean searchLocalCache)
    {
        if (address == null)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        URL cacheFileUrl = null;

        if (address.trim().startsWith("jar:"))
        {
            URL jarUrl = WWIO.makeURL(address); // retrieval URL may be other than the address' URL
            if (WWIO.isLocalJarAddress(jarUrl))
            {
                if (this.getJarLength(jarUrl) > 0)
                    cacheFileUrl = jarUrl;
                else
                {
                    getAbsentResourceList().markResourceAbsent(address);
                    return null;
                }
            }
        }

        String addressProtocol = retrievalUrl != null ? retrievalUrl.getProtocol() : null;
        if (cacheFileUrl == null && (addressProtocol == null || addressProtocol.equals("file")))
        {
            File f = new File(address);
            if (f.exists())
                try
                {
                    cacheFileUrl = f.toURI().toURL();  // makes a file URL
                }
                catch (MalformedURLException e)
                {
                    // The toURL call shouldn't fail, but continue on if it does.
                }
        }

        // If the address is a file, look for the file in the classpath and WorldWind disk cache. We perform this step
        // regardless of the searchLocalCache parameter, because this looks for the file in the classpath.
        // We need to ensure that the address is not a network address (HTTP, etc.) because the getResource call in
        // findFile will attempt to retrieve from that URL on the thread that called this method, which might be the EDT
        // (See WWJ-434).
        if (cacheFileUrl == null && (addressProtocol == null || addressProtocol.equals("file")))
            cacheFileUrl = WorldWind.getDataFileStore().findFile(address, true);

        // Look for the file in the WorldWind disk cache by creating a cache path from the file's address. We ignore this
        // step if searchLocalCache is false.
        if (cacheFileUrl == null && retrievalUrl != null && searchLocalCache)
        {
            String cachePath = this.makeCachePath(retrievalUrl, null);
            cacheFileUrl = WorldWind.getDataFileStore().findFile(cachePath, true);

            // If a address is requested that does not have a format suffix, then any previous call to makeLocal for the
            // same address has appended a suffix to the file's cache path that is appropriate for the content type
            // returned by the server. This means the address cannot be used to locate that file without knowing the
            // content type. We use this file store's configurable cache content types to guess the file's content type
            // and located it in the cache. Note that if the address has a suffix but is simply not in the cache, we do
            // not attempt to locate it by guessing its content type.
            String suffix = WWIO.getSuffix(cachePath);
            if (cacheFileUrl == null && (suffix == null || suffix.length() > 4))
            {
                for (String contentType : this.getCacheContentTypes())
                {
                    String pathWithSuffix = cachePath + WWIO.makeSuffixForMimeType(contentType);
                    cacheFileUrl = WorldWind.getDataFileStore().findFile(pathWithSuffix, true);
                    if (cacheFileUrl != null)
                        break;
                }
            }
        }

        if (cacheFileUrl != null)
        {
            DBEntry entry = new DBEntry(address);
            entry.localUrl = cacheFileUrl;
            entry.state = DBEntry.LOCAL;
            entry.contentType = WWIO.makeMimeTypeForSuffix(WWIO.getSuffix(cacheFileUrl.getPath()));
            this.db.add(address, entry);
            this.getAbsentResourceList().unmarkResourceAbsent(address);

            return cacheFileUrl;
        }

        return null;
    }

    /**
     * Returns the length of the resource referred to by a jar URL. Can be used to test whether the resource exists.
     * <p>
     * Note: This method causes the URL to open a connection and retrieve content length.
     *
     * @param jarUrl the jar URL.
     *
     * @return the jar file's content length, or -1 if a connection to the URL can't be formed or queried.
     */
    protected int getJarLength(URL jarUrl)
    {
        try
        {
            return jarUrl.openConnection().getContentLength();
        }
        catch (IOException e)
        {
            String message = Logging.getMessage("generic.JarOpenFailed", jarUrl.toString());
            Logging.logger().log(java.util.logging.Level.WARNING, message, e);

            return -1;
        }
    }

    /**
     * Retrieves a specified file and either adds it to the cache or saves it in a temporary file, depending on the
     * value of <code>saveInLocalCache</code>.
     *
     * @param address          the name used to identify the cached file.
     * @param url              the URL to obtain the file.
     * @param saveInLocalCache <code>true</code> to add the file to the cache, or <code>false</code> to save it in a
     *                         temporary location.
     */
    protected synchronized void makeLocal(String address, URL url, boolean saveInLocalCache)
    {
        if (WorldWind.getNetworkStatus().isHostUnavailable(url) || !WorldWind.getRetrievalService().isAvailable())
            return;

        DBEntry newEntry = new DBEntry(address);
        this.db.add(address, newEntry);
        newEntry.state = DBEntry.PENDING;

        Retriever retriever = URLRetriever.createRetriever(url, new PostProcessor(address, url, saveInLocalCache));

        if (retriever != null && !WorldWind.getRetrievalService().contains(retriever))
            WorldWind.getRetrievalService().runRetriever(retriever);
    }

    protected class PostProcessor extends AbstractRetrievalPostProcessor
    {
        protected String address;
        protected URL retrievalUrl;
        protected URL localFileUrl = null;
        protected File outputFile = null;
        protected boolean saveInLocalCache;

        public PostProcessor(String address, URL url, boolean saveInLocalCache)
        {
            this.address = address;
            this.retrievalUrl = url;
            this.saveInLocalCache = saveInLocalCache;
        }

        @Override
        protected boolean overwriteExistingFile()
        {
            return true;
        }

        protected File doGetOutputFile()
        {
            // Create the output file once and cache the result to avoid creating unused temporary files. If this
            // PostProcessor's saveInLocalCache method is false, then make makeOutputFile creates a unique temporary
            // file on each call. Since this method is potentially called multiple times by
            // AbstractRetrievalPostProcessor, we call makeOutputFile at most one time so that only one temporary output
            // file is created.
            if (this.outputFile == null)
                this.outputFile = this.makeOutputFile();

            return this.outputFile;
        }

        protected File makeOutputFile()
        {
            File file;

            String path = makeCachePath(this.retrievalUrl, this.getRetriever().getContentType());
            if (this.saveInLocalCache && path.length() <= WWIO.MAX_FILE_PATH_LENGTH)
                file = WorldWind.getDataFileStore().newFile(path);
            else
                file = BasicDataFileStore.this.makeTempFile(this.retrievalUrl, this.getRetriever().getContentType());

            if (file == null)
                return null;

            try
            {
                this.localFileUrl = file.toURI().toURL();
                return file;
            }
            catch (MalformedURLException e)
            {
                String message = Logging.getMessage("generic.MalformedURL", file.toURI());
                Logging.logger().finest(message);
                return null;
            }
        }

        @Override
        protected boolean saveBuffer() throws IOException
        {
            boolean tf = super.saveBuffer();
            BasicDataFileStore.this.updateEntry(this.address, this.localFileUrl,
                this.getRetriever().getExpirationTime());
            return tf;
        }

        @Override
        protected ByteBuffer handleSuccessfulRetrieval()
        {
            ByteBuffer buffer = super.handleSuccessfulRetrieval();

            firePropertyChange(
                new PropertyChangeEvent(BasicDataFileStore.this, AVKey.RETRIEVAL_STATE_SUCCESSFUL, this.retrievalUrl,
                    this.localFileUrl));

            return buffer;
        }

        @Override
        protected void markResourceAbsent()
        {
            BasicDataFileStore.this.getAbsentResourceList().markResourceAbsent(this.address);
        }

        /** {@inheritDoc} Overridden to save text files in the cache. */
        @Override
        protected ByteBuffer handleTextContent() throws IOException
        {
            this.saveBuffer();

            return this.getRetriever().getBuffer();
        }
    }

    /**
     * Updates a cache entry with information available once the file is retrieved.
     *
     * @param address      the name used to identify the file in the cache.
     * @param localFileUrl the path to the local copy of the file.
     * @param expiration   time (in milliseconds since the Epoch) at which this entry expires, or zero to indicate that
     *                     there is no expiration time.
     */
    protected synchronized void updateEntry(String address, URL localFileUrl, long expiration)
    {
        DBEntry entry = (DBEntry) this.db.getObject(address);
        if (entry == null)
            return;

        entry.state = DBEntry.LOCAL;
        entry.localUrl = localFileUrl;
        entry.contentType = WWIO.makeMimeTypeForSuffix(WWIO.getSuffix(localFileUrl.getPath()));
        entry.expiration = expiration;
        entry.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Makes a path to the file in the cache from the file's URL and content type.
     *
     * @param url         the URL to obtain the file.
     * @param contentType the mime type of the file's contents.
     *
     * @return a path name.
     */
    protected String makeCachePath(URL url, String contentType)
    {
        if ("jar".equals(url.getProtocol()))
            return this.makeJarURLCachePath(url, contentType);

        return this.makeGenericURLCachePath(url, contentType);
    }

    /**
     * Makes a path to the file in the cache from the file's generic URL and content type. If the URL has a non-empty
     * query string, then this returns a path name formatted as follows:
     * <p>
     * <code>host/hashCode/path_query.suffix</code>
     * <p>
     * Otherwise, this returns a path name formatted as follows:
     * <p>
     * <code>host/hashCode/path.suffix</code>
     * <p>
     * Where <code>host</code> is the name of the host machine, <code>hashCode</code> is a four digit hash code computed
     * from the string "path" or "path_query" (if the URL has a query string), <code>path</code> is the URL's path part,
     * <code>query</code> is the URL's query string, and <code>suffix</code> is either the path's suffix or a suffix
     * created from the specified content type. The <code>hashCode</code> folder is used to limit the number of files
     * that appear beneath the host folder. This is necessary to avoiding the operating system's maximum file limit
     * should a large number of files be requested from the same host. If two URLs have the same hash code, then both
     * URLs are stored under the same <code>hashCode</code> folder in the cache and are differentiated by their
     * <code>path</code> and <code>query</code> parts.
     * <p>
     * This removes any private parameters from the query string to ensure that those parameters are not written to the
     * file store as part of the cache name. For example, the "CONNECTID" query parameter typically encodes a user's
     * unique connection id, and must not be shared. Writing this parameter to the cache would expose that parameter to
     * anyone using the same machine. If the query string is empty after removing any private parameters, it is ignored
     * and only the path part of the URL is used as the filename.
     *
     * @param url         the URL to obtain the file.
     * @param contentType the mime type of the file's contents.
     *
     * @return a path name.
     */
    protected String makeGenericURLCachePath(URL url, String contentType)
    {
        String host = WWIO.replaceIllegalFileNameCharacters(url.getHost());
        String path = WWIO.replaceIllegalFileNameCharacters(url.getPath());
        String filename = path;

        if (!WWUtil.isEmpty(url.getQuery()))
        {
            // Remove private query parameters from the query string, and replace any illegal filename characters with
            // an underscore. This avoids exposing private parameters to other users by writing them to the cache as
            // part of the cache name.
            String query = this.removePrivateQueryParameters(url.getQuery());
            query = WWIO.replaceIllegalFileNameCharacters(query);

            // If the query string is not empty after removing private parameters and illegal filename characters, we
            // use it as part of the cache name by appending it to the path part.
            if (!WWUtil.isEmpty(query))
            {
                filename = path + "_" + query;
            }
        }

        // Create a hash folder name using the first four numbers of the filename's hash code (ignore any negative
        // sign). The filename is either the path name or the path name appended with the query string. In either case,
        // the same hash folder name can be re-created from the same address. If two URLs have the same hash string,
        // both URLs are stored under the same hash folder and are differentiated by their filenames.
        String hashString = String.valueOf(Math.abs(filename.hashCode()));
        if (hashString.length() > 4)
            hashString = hashString.substring(0, 4);

        StringBuilder sb = new StringBuilder();
        sb.append(host);
        sb.append(File.separator);
        sb.append(hashString);
        sb.append(File.separator);
        sb.append(filename);

        String suffix = this.makeSuffix(filename, contentType);
        if (suffix != null)
            sb.append(suffix);

        return sb.toString();
    }

    /**
     * Makes a path to the file in the cache from the file's JAR URL and content type. This returns a path name
     * formatted as follows:
     * <p>
     * <code>host/path.suffix</code>
     * <p>
     * Where <code>host</code> is the path to the JAR file, <code>path</code> is the file's path within the JAR archive,
     * and <code>suffix</code> is either the path's suffix or a suffix created from the specified content type.
     *
     * @param jarURL      the URL to obtain the file. This URL is assumed to have the "jar" protocol.
     * @param contentType the mime type of the file's contents.
     *
     * @return a path name.
     */
    protected String makeJarURLCachePath(URL jarURL, String contentType)
    {
        String innerAddress = jarURL.getPath();
        URL innerUrl = WWIO.makeURL(innerAddress);
        String host = WWIO.replaceIllegalFileNameCharacters(innerUrl.getHost());
        String path = WWIO.replaceIllegalFileNameCharacters(innerUrl.getPath().replace("!/", "#"));

        StringBuilder sb = new StringBuilder();
        sb.append(host);
        sb.append(File.separator);
        sb.append(path);

        String suffix = this.makeSuffix(path, contentType);
        if (suffix != null)
            sb.append(suffix);

        return sb.toString();
    }

    /**
     * Creates a temp file to hold the contents associated with a specified URL. Since the file store intentionally does
     * not persist a mapping of retrieved URLs to temp files, this deletes the returned temp file when the current Java
     * Virtual Machine terminates.
     *
     * @param url         the URL to be associated with the temp file. Used only to determine an appropriate suffix.
     * @param contentType the mime type of the file contents. Used to determine the file's suffix.
     *
     * @return a temporary file, or null if a file could not be created.
     */
    protected File makeTempFile(URL url, String contentType)
    {
        // Use a suffix based on the content type if the content type and the URL's suffix do not match. Otherwise
        // attempt to use the URL's suffix. If neither of these attempts produce a non-null suffix, File.createTmpFile
        // uses the default suffix ".tmp".
        String suffix = this.makeSuffix(url.toString(), contentType); // null if the URL suffix and content type match.
        if (suffix == null)
            suffix = WWIO.getSuffix(url.toString());

        // Ensure that the suffix starts with the "." character.
        if (!suffix.startsWith("."))
            suffix = "." + suffix;

        try
        {
            File file = File.createTempFile("wwfs", suffix); // Uses the default suffix ".tmp" if this suffix is null.
            file.deleteOnExit();
            return file;
        }
        catch (IOException e)
        {
            String message = Logging.getMessage("generic.CannotCreateTempFile");
            Logging.logger().fine(message);
            return null;
        }
    }

    /**
     * Determines an appropriate suffix for a cached file. If the specified path already has a suffix that matches the
     * specified content type, then this method returns null. Otherwise the method determines and returns a suffix for
     * the specified content type.
     *
     * @param path        the path whose suffix is to be validated if it exists.
     * @param contentType the mime type of the data associated with the path.
     *
     * @return a suffix appropriate to the content type, or null if the specified path already has an appropriate
     *         suffix.
     */
    protected String makeSuffix(String path, String contentType)
    {
        // If the cache path does not end in a suffix that matches the specified content type, we append the appropriate
        // suffix. If the content type is not known, we do not append any suffix. If the caller does not know the
        // content type used to create a cache file path, it must attempt to use known mime types until it finds a
        // match.
        String suffix = contentType != null ? WWIO.makeSuffixForMimeType(contentType) : null;
        String existingSuffix = WWIO.getSuffix(path);

        // The suffix returned by makeSuffixForMimeType is always ".jpg" for a JPEG mime type. We must convert any
        // existing using "jpeg" to "jpg" to correctly match against the suffix created from the content type.
        if (existingSuffix != null && existingSuffix.equalsIgnoreCase("jpeg"))
            existingSuffix = "jpg";

        if (suffix != null && (existingSuffix == null || !existingSuffix.equalsIgnoreCase(suffix.substring(1))))
            return suffix;
        else
            return null;
    }

    /**
     * Removes any private parameters from the query string to ensure that those parameters are not written to the file
     * store as part of the cache name. For example, the "CONNECTID" query parameter typically encodes a user's unique
     * connection id, and must not be shared. Writing this parameter to the cache would expose that parameter to anyone
     * using the same machine.
     * <p>
     * This removes the key, the value, and any trailing parameter delimiter of all private parameters in the specified
     * query string. Recognized private query parameters are as follows:
     * <ul> <li>CONNECTID</li> </ul>
     *
     * @param queryString the query string to examine.
     *
     * @return a new string with the private query parameters removed. This string is empty if the query string is
     *         empty, or if the query string contains only private parameters.
     */
    protected String removePrivateQueryParameters(String queryString)
    {
        if (WWUtil.isEmpty(queryString))
            return queryString;

        // Remove the "connectid" query parameter, its corresponding value, and any trailing parameter delimiter. We
        // specify the regular expression directive "(?i)" to enable case-insensitive matching. The regular expression
        // parameters "\Q" and "\E" define the begin and end of a literal quote around the query parameter name.
        String s = queryString.replaceAll("(?i)\\Qconnectid\\E\\=[^&]*\\&?", "");

        // If we removed the query string's last parameter, we need to clean up the trailing delimiter from the previous
        // query parameter.
        if (s.endsWith("&"))
            s = s.substring(0, s.length() - 1);

        return s;
    }
}
