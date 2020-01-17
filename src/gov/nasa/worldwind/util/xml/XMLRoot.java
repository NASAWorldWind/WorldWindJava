package gov.nasa.worldwind.util.xml;

public interface XMLRoot {

    /**
     * Finds a named element in the document.
     *
     * @param id the element's identifier. If null, null is returned.
     *
     * @return the element requested, or null if there is no corresponding element in the document.
     */
    public Object getItemByID(String id);

    /**
     * Resolves a reference to a local element identified by address and identifier, where {@code linkBase} identifies a
     * document, including the current document, and {@code linkRef} is the id of the desired element.
     * <p>
     * If {@code linkBase} refers to a local KML or KMZ file and {@code linkRef} is non-null, the return value is the
     * element identified by {@code linkRef}. If {@code linkRef} is null, the return value is a parsed {@link KMLRoot}
     * for the KML file identified by {@code linkBase}.
     * <p>
     * If {@code linkBase} refers a local file that is not a KML or KMZ file then {@code linkBase} is returned. If
     * {@code linkBase} cannot be resolved to a local file then null is returned.
     *
     * @param linkBase the address of the document containing the requested element.
     * @param linkRef the element's identifier.
     *
     * @return the requested element, or null if the element is not found.
     *
     * @throws IllegalArgumentException if the address is null.
     */
    public Object resolveLocalReference(String linkBase, String linkRef);

    /**
     * Resolves a reference to a remote element identified by address and identifier, where {@code linkBase} identifies
     * a remote document, and {@code linkRef} is the id of the desired element. This method retrieves resources
     * asynchronously using the {@link gov.nasa.worldwind.cache.FileStore}.
     * <p>
     * The return value is null if the file is not yet available in the FileStore. If {@code linkBase} refers to a KML
     * or KMZ file and {@code linkRef} is non-null, the return value is the element identified by {@code linkRef}. If
     * {@code linkBase} refers to a KML or KMZ and {@code linkRef} is null, the return value is a parsed {@link KMLRoot}
     * for the KML file identified by {@code linkBase}. Otherwise the return value is a {@link URL} to the file in the
     * file cache or a temporary location, depending on the value of <code>cacheRemoteFile</code>.
     * <p>
     * The <code>cacheRemoteFile</code> parameter specifies whether to store a retrieved remote file in the WorldWind
     * cache or in a temporary location. This parameter has no effect if the file exists locally. The temporary location
     * for a retrieved file does not persist between runtime sessions, and subsequent invocations of this method may not
     * return the same temporary location.
     *
     * @param linkBase        the address of the document containing the requested element.
     * @param linkRef         the element's identifier.
     * @param cacheRemoteFile <code>true</code> to store remote files in the WorldWind cache, or <code>false</code> to
     *                        store remote files in a temporary location. Has no effect if the address is a local file.
     *
     * @return URL to the requested file, parsed KMLRoot, or KML feature. Returns null if the document is not yet
     *         available in the FileStore.
     *
     * @throws IllegalArgumentException if the {@code linkBase} is null.
     */
    public Object resolveRemoteReference(String linkBase, String linkRef, boolean cacheRemoteFile);
}
