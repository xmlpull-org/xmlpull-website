PullParser 2 driver for Xerces 2.0.1 will work with unpatched 
Xerces 2.0.1 however when this patch is applied the driver
will be able to report event positions and will pass all XPP2
internal tests.

Apply the included PATCH file or simply use provided patched JAR 
(it also fixes bug reported to Xerces 2 mailing list that prevents
passing XPP2 tests - see patch for src/org/apache/xerces/impl/XMLNamespaceBinder.java)
