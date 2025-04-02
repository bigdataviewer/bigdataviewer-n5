/*
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

import static mpicbg.spim.data.XmlKeys.IMGLOADER_FORMAT_ATTRIBUTE_NAME;

import java.io.File;
import java.net.URI;

import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.universe.N5Factory;
import org.janelia.saalfeldlab.n5.universe.StorageFormat;
import org.jdom2.Element;

import mpicbg.spim.data.XmlHelpers;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.ImgLoaderIo;
import mpicbg.spim.data.generic.sequence.XmlIoBasicImgLoader;

@ImgLoaderIo( format = "bdv.n5.cloud", type = N5CloudImageLoader.class )
public class XmlIoN5CloudImageLoader implements XmlIoBasicImgLoader< N5CloudImageLoader >
{
	@Override
	public Element toXml( final N5CloudImageLoader imgLoader, final File basePath )
	{
		return toXml( imgLoader, basePath.toURI() );
	}

	@Override
	public Element toXml( final N5CloudImageLoader imgLoader, final URI basePath )
	{
		final Element elem = new Element( "ImageLoader" );
		elem.setAttribute( IMGLOADER_FORMAT_ATTRIBUTE_NAME, "bdv.n5.cloud" );
		elem.setAttribute( "version", "0.1" );
		elem.addContent( XmlHelpers.pathElementURI( "n5", imgLoader.getN5URI(), basePath ) );
		return elem;
	}

	@Override
	public N5CloudImageLoader fromXml( final Element elem, final File basePath, final AbstractSequenceDescription< ?, ?, ? > sequenceDescription )
	{
		return fromXml( elem, basePath.toURI(), sequenceDescription );
	}

	@Override
	public N5CloudImageLoader fromXml( final Element elem, final URI basePathURI, final AbstractSequenceDescription< ?, ?, ? > sequenceDescription )
	{
//		final String version = elem.getAttributeValue( "version" );
		final URI uri = XmlHelpers.loadPathURI( elem, "n5", basePathURI );
		//final N5Reader n5Reader = N5Factory.createReader( uri.toString() );

		N5Reader n5Reader;

		try
		{
			//System.out.println( "Trying reading with credentials ..." );
			N5Factory factory = new N5Factory();
			factory.s3UseCredentials();
			n5Reader = factory.openReader( StorageFormat.N5, uri );
		}
		catch ( Exception e )
		{
			//System.out.println( "With credentials failed; trying anonymous ..." );
			n5Reader = new N5Factory().openReader( StorageFormat.N5, uri );
		}

		return new N5CloudImageLoader( n5Reader, uri, sequenceDescription );
	}
}
