/*-
 * #%L
 * BigDataViewer backend for N5, Zarr, etc.
 * %%
 * Copyright (C) 2024 - 2025 BigDataViewer developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.bigdataviewer.n5;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import org.janelia.saalfeldlab.googlecloud.GoogleCloudUtils;
import org.janelia.saalfeldlab.n5.GsonKeyValueN5Reader;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.KeyValueAccess;
import org.janelia.saalfeldlab.n5.s3.AmazonS3Utils;
import org.janelia.saalfeldlab.n5.universe.N5Factory;
import org.janelia.saalfeldlab.n5.universe.StorageFormat;

// TODO find a better name
// TODO plugin mechanism similar to n5-universe?
public class URIAccessHelper
{
	// TODO add to KeyValueAccess interface
	public static class KeyValueAccessWithRootURI
	{
		private final URI root;

		private final KeyValueAccess kva;

		public KeyValueAccessWithRootURI( URI root, KeyValueAccess kva )
		{
			this.root = root;
			this.kva = kva;
		}

		public URI getRootURI()
		{
			return root;
		}

		public KeyValueAccess getKeyValueAccess()
		{
			return kva;
		}

		public Reader openReader( final URI uri ) throws IOException
		{
			final String path = root.relativize( uri ).getPath();
			final String normalPath = kva.normalize( kva.compose( root, path ) );
			return kva.lockForReading( normalPath ).newReader();
		}
	}

	static KeyValueAccessWithRootURI createKeyValueAccess( final URI uri ) throws IOException
	{
		try
		{
			final KeyValueAccess kva = getKeyValueAccessFor( kvaRootURI( uri ) );
			final URI base = kvaRootURI( uri );
			return new KeyValueAccessWithRootURI( base, kva );
		}
		catch ( URISyntaxException e )
		{
			throw new IOException( e );
		}
	}

	private static KeyValueAccess getKeyValueAccessFor( URI uri )
	{
		final N5Reader n5r = new N5Factory().openReader( StorageFormat.N5, uri );
		return ( ( GsonKeyValueN5Reader ) n5r ).getKeyValueAccess();
	}

	private static URI kvaRootURI( URI uri ) throws URISyntaxException
	{
		if ( isGC( uri ) )
		{
			throw new UnsupportedOperationException( "TODO" );
		}
		else if ( isS3( uri ) )
		{
			return new URI( "s3://" + AmazonS3Utils.getS3Bucket( uri ) );
		}
		else if ( isFile( uri ) )
		{
			return new URI( "file:///" );
		}

		throw new UnsupportedOperationException( "TODO" );
	}

	//  TODO: copied from n5-universe. revise

	private final static Pattern HTTPS_SCHEME = Pattern.compile( "http(s)?", Pattern.CASE_INSENSITIVE );

	private final static Pattern FILE_SCHEME = Pattern.compile( "file", Pattern.CASE_INSENSITIVE );

	private static boolean isGC( URI uri )
	{
		final String scheme = uri.getScheme();
		final boolean hasScheme = scheme != null;
		if ( !hasScheme )
			return false;
		if ( GoogleCloudUtils.GS_SCHEME.asPredicate().test( scheme ) )
			return true;
		return uri.getHost() != null && HTTPS_SCHEME.asPredicate().test( scheme ) && GoogleCloudUtils.GS_HOST.asPredicate().test( uri.getHost() );
	}

	private static boolean isS3( URI uri )
	{
		final String scheme = uri.getScheme();
		final boolean hasScheme = scheme != null;
		if ( !hasScheme )
			return false;
		if ( AmazonS3Utils.S3_SCHEME.asPredicate().test( scheme ) )
			return true;
		return uri.getHost() != null && HTTPS_SCHEME.asPredicate().test( scheme );
	}

	private static boolean isFile( URI uri )
	{
		final String scheme = uri.getScheme();
		final boolean hasScheme = scheme != null;
		return !hasScheme || FILE_SCHEME.asPredicate().test( scheme );
	}
}
