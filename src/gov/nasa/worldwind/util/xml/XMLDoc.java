package gov.nasa.worldwind.util.xml;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;

public abstract class XMLDoc {

    /**
     * Returns an {@link java.io.InputStream} to the associated XML document.
     * <p>
     * Implementations of this interface do not close the stream; the user of the class must close the stream.
     *
     * @return an input stream positioned to the head of the XML document.
     *
     * @throws java.io.IOException if an error occurs while attempting to create or open the input stream.
     */
    public abstract InputStream getInputStream() throws IOException;

    /**
     * Returns an absolute path or URL to a file indicated by a path relative to the XML file's location.
     *
     * @param path the path of the requested file.
     *
     * @return an absolute path or URL to the file, or null if the file does not exist.
     *
     * @throws IllegalArgumentException if the specified path is null.
     * @throws java.io.IOException if an error occurs while attempting to read the support file.
     */
    public abstract String getSupportFilePath(String path) throws IOException;

    /**
     * Returns a file specified by a path relative to the XML document. If the document is in a KML file, the path is
     * resolved relative to the KML file's location in the file system. If the document is in a KMZ file or stream, the
     * path is resolved relative to the root of the KMZ file or stream. If the document is a KML stream, the relative
     * path is resolved relative to the base URI of the stream, if a base URI has been specified.
     *
     * @param path the path of the requested file.
     *
     * @return an input stream positioned to the start of the requested file, or null if the file cannot be found.
     *
     * @throws IllegalArgumentException if the path is null.
     * @throws IOException if an error occurs while attempting to create or open the input stream.
     */
    public abstract InputStream getSupportFileStream(String path) throws IOException;

    /**
     * Resolves a reference to a remote or local element of the form address#identifier, where "address" identifies a
     * local or remote document, including the current document, and and "identifier" is the id of the desired element.
     * <p>
     * If the address part identifies the current document, the document is searched for the specified identifier.
     * Otherwise the document is retrieved, opened and searched for the identifier. If the address refers to a remote
     * document and the document has not previously been retrieved and cached locally, retrieval is initiated and this
     * method returns <code>null</code>. Once the document is successfully retrieved, subsequent calls to this method
     * return the identified element, if it exists.
     * <p>
     * If the link does not contain an identifier part, this initiates a retrieval for document referenced by the
     * address part and returns <code>null</code>. Once the document is retrieved this opens the the document as a
     * <code>KMLRoot</code>. Subsequent calls to this method return the opened document, if it exists.
     * <p>
     * The <code>cacheRemoteFile</code> parameter specifies whether to store a retrieved remote document in the World
     * Wind cache or in a temporary location. This parameter has no effect if the document exists locally. The temporary
     * location for a retrieved document does not persist between runtime sessions, and subsequent invocations of this
     * method may not return the same temporary location.
     *
     * @param root Document root
     * @param link the document address in the form address#identifier.
     * @param cacheRemoteFile <code>true</code> to store remote documents in the WorldWind cache, or <code>false</code>
     * to store remote documents in a temporary location. Has no effect if the address is a local document.
     *
     * @return the requested document, the requested or element within a document, or <code>null</code> if the document
     * or the element are not found.
     *
     * @throws IllegalArgumentException if the <code>link</code> is <code>null</code>.
     */
    public Object resolveReference(XMLRoot root, String link, boolean cacheRemoteFile) {
        if (link == null) {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try {
            String[] linkParts = link.split("#");
            String linkBase = linkParts[0];
            String linkRef = linkParts.length > 1 ? linkParts[1] : null;

            // See if it's a reference to an internal element.
            if (WWUtil.isEmpty(linkBase) && !WWUtil.isEmpty(linkRef)) {
                return root.getItemByID(linkRef);
            }

            if (linkBase.startsWith("./") || linkBase.startsWith(".\\")) {
                linkBase = linkBase.substring(2);
            }
            // Interpret the path relative to the current document.
            String path = this.getSupportFilePath(linkBase);
            if (path == null) {
                path = linkBase;
            }

            // See if it's an already found and parsed KML file.
            Object o = WorldWind.getSessionCache().get(path);
            if (o != null && o instanceof XMLRoot) {
                return linkRef != null ? ((XMLRoot) o).getItemByID(linkRef) : o;
            }

            URL url = WWIO.makeURL(path);
            if (url == null) {
                // See if the reference can be resolved to a local file.
                o = root.resolveLocalReference(path, linkRef);
            }

            // If we didn't find a local file, treat it as a remote reference.
            if (o == null) {
                o = root.resolveRemoteReference(path, linkRef, cacheRemoteFile);
            }

            if (o != null) {
                return o;
            }

            // If the reference was not resolved as a remote reference, look for a local element identified by the
            // reference string. This handles the case of malformed internal references that omit the # sign at the
            // beginning of the reference.
            return root.getItemByID(link);
        } catch (Exception e) {
            String message = Logging.getMessage("generic.UnableToResolveReference", link);
            Logging.logger().warning(message);
        }

        return null;
    }
}
