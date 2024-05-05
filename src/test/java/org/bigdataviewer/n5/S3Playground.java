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


