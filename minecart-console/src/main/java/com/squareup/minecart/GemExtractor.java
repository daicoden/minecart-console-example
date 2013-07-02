/**
 This file is taken directly from warbler with slight modifications.

 https://github.com/jruby/warbler/blob/82415016cfcba5399d9f68f832bc3fd8e38ccebe/ext/JarMain.java

 - Licence -

 = Warbler

 Warbler is provided under the terms of the MIT license.

 Warbler (c) 2010-2013 Engine Yard, Inc.
 Warbler (c) 2007-2009 Sun Microsystems, Inc.

 Permission is hereby granted, free of charge, to any person
 obtaining a copy of this software and associated documentation files
 (the "Software"), to deal in the Software without restriction,
 including without limitation the rights to use, copy, modify, merge,
 publish, distribute, sublicense, and/or sell copies of the Software,
 and to permit persons to whom the Software is furnished to do so,
 subject to the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package com.squareup.minecart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class GemExtractor {
  static final String MAIN = "/" + GemExtractor.class.getName().replace('.', '/') + ".class";

  private File extractRoot;
  private static boolean debug = true;
  protected final String archive;
  private final String path;

  public GemExtractor() {
    URL mainClass = getClass().getResource(MAIN);
    try {
      this.path = mainClass.toURI().getSchemeSpecificPart();
    }
    catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    archive = this.path.replace("!" + MAIN, "").replace("file:", "");
  }

  protected String extractGems() throws Exception {
    final JarFile jarFile = new JarFile(archive);
    try {
      Map<String, JarEntry> jarNames = new HashMap<String, JarEntry>();
      for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); ) {
        JarEntry entry = e.nextElement();
        String extractPath = getExtractEntryPath(entry);

        if ( extractPath != null ) jarNames.put(extractPath, entry);
      }

      extractRoot = File.createTempFile("jruby", "extract");
      extractRoot.delete(); extractRoot.mkdirs();

      final List<URL> urls = new ArrayList<URL>();
      for (Map.Entry<String, JarEntry> e : jarNames.entrySet()) {
        URL entryURL = extractEntry(e.getValue(), e.getKey());
        if (entryURL != null) urls.add( entryURL );
      }

      return extractRoot.getPath();
    }
    finally {
      jarFile.close();
    }
  }

  protected String getExtractEntryPath(final JarEntry entry) {
    final String name = entry.getName();
    if ( name.startsWith("rubygems/") ) {
      return name.substring(name.indexOf("/"), name.length());
    }
    return null; // do not extract entry
  }

  protected URL extractEntry(final JarEntry entry, final String path) throws Exception {
    final File file = new File(extractRoot, path);
    if ( entry.isDirectory() ) {
      file.mkdirs();
      return null;
    }
    final String entryPath = entryPath(entry.getName());
    final InputStream entryStream;
    try {
      entryStream = new URI("jar", entryPath, null).toURL().openStream();
    }
    catch (IllegalArgumentException e) {
      // TODO gems '%' file name "encoding" ?!
      debug("failed to open jar:" + entryPath + " skipping entry: " + entry.getName(), e);
      return null;
    }
    final File parent = file.getParentFile();
    if ( parent != null ) parent.mkdirs();
    FileOutputStream outStream = new FileOutputStream(file);
    final byte[] buf = new byte[65536];
    try {
      int bytesRead = 0;
      while ((bytesRead = entryStream.read(buf)) != -1) {
        outStream.write(buf, 0, bytesRead);
      }
    }
    finally {
      entryStream.close();
      outStream.close();
      file.deleteOnExit();
    }
    if (false) debug(entry.getName() + " extracted to " + file.getPath());
    return file.toURI().toURL();
  }

  protected void debug(String msg) {
    debug(msg, null);
  }

  protected String entryPath(String name) {
    if ( ! name.startsWith("/") ) name = "/" + name;
    return path.replace(MAIN, name);
  }

  protected void debug(String msg, Throwable t) {
    if (debug) System.out.println(msg);
    if (debug && t != null) t.printStackTrace(System.out);
  }
}
