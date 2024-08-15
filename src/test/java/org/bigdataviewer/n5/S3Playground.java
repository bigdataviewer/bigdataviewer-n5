/*-
 * #%L
 * BigDataViewer backend for N5, Zarr, etc.
 * %%
 * Copyright (C) 2024 BigDataViewer developers.
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

import bdv.BigDataViewer;
import bdv.ViewerImgLoader;
import bdv.export.ProgressWriterConsole;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.ui.UIUtils;
import bdv.viewer.ViewerOptions;
import mpicbg.spim.data.SpimDataException;

public class S3Playground
{
	public static void main( String[] args ) throws IOException, SpimDataException
	{
		final URI xml = URI.create( "s3://janelia-bigstitcher-spark/Stitching/dataset.xml" );

		System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		UIUtils.installFlatLafInfos();

		final URIAccessHelper.KeyValueAccessWithRootURI kva = URIAccessHelper.createKeyValueAccess( xml );

		// We need to pass an absolute URI to XmlIoSpimDataMinimal.load() (in particular, prefixed with "file:/" for filesystem paths)
		final URI xmlURI = kva.getRootURI().resolve( xml );

		final Reader xmlReader = kva.openReader( xmlURI );
		final SpimDataMinimal spimData = new XmlIoSpimDataMinimal().load( xmlReader, xmlURI );
		( ( ViewerImgLoader ) spimData.getSequenceDescription().getImgLoader() ).setNumFetcherThreads( 256 );

		BigDataViewer.open( spimData, "s3 bdv test", new ProgressWriterConsole(), ViewerOptions.options() );
	}
}


