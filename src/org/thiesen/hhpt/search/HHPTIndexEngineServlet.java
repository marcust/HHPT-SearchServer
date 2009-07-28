/*
 * $ Id $
 * (c) Copyright 2009 freiheit.com technologies gmbh
 *
 * This file contains unpublished, proprietary trade secret information of
 * freiheit.com technologies gmbh. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * freiheit.com technologies gmbh.
 *
 * Initial version by Marcus Thiesen (marcus.thiesen@freiheit.com)
 */
package org.thiesen.hhpt.search;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.NumericUtils;
import org.apache.tools.bzip2.CBZip2OutputStream;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.thiesen.hhpt.search.beans.SerializedRamDirectory;
import org.thiesen.hhpt.search.service.PMF;

import com.google.appengine.api.datastore.Key;

public class HHPTIndexEngineServlet extends BaseServlet {

    private static final long serialVersionUID = 5425673143683167896L;
    private static final String ADD = "add";
    private static final String DELETE = "delete";

    @Override
    protected void doPost( final HttpServletRequest req, @SuppressWarnings( "unused" ) final HttpServletResponse resp ) throws ServletException, IOException {
        final InputStream inStream = req.getInputStream();
        try {
            final Element rootElement = getRootElement( inStream );
            
            final String name = rootElement.getName();
            
            if ( ADD.equals( name  ) ) {
                add( rootElement );    
            } 
            if ( DELETE.equals( name ) ) {
                delete( rootElement );
            }
            
        } catch ( final JDOMException e ) {
            throw new ServletException( e );
        } catch ( final ClassNotFoundException e ) {
            throw new ServletException( e );
        }
            
        
    }

    private void delete( @SuppressWarnings( "unused" ) final Element rootElement ) {
        throw new UnsupportedOperationException("Auto generated method stub");
        
    }

    @SuppressWarnings( "unchecked" )
    private void add( final Element rootElement ) throws CorruptIndexException, LockObtainFailedException, IOException, ClassNotFoundException {
        final List<Element> documentElements = rootElement.getChildren( "doc" );
        
        final RAMDirectory directory = loadDirectory();
        final IndexWriter writer = new IndexWriter( directory, new WhitespaceAnalyzer(), MaxFieldLength.UNLIMITED );
        writer.setMergeScheduler( new SerialMergeScheduler() );
        
        for ( final Element docElement : documentElements ) {
            writer.addDocument( convert( docElement ) );
        }
        
        writer.optimize();
        
        writer.close();
        
        storeDirectory( directory );
    }

    
    private void storeDirectory( final RAMDirectory directory ) throws IOException {
        final PersistenceManager pm = PMF.get().getPersistenceManager();


        final byte[] bytes = serialize( directory );
        
        
        final Key k = makeIndexKey();
        
        final SerializedRamDirectory serializedDirectory = new SerializedRamDirectory( k, bytes );
        
        try {
        
            pm.makePersistent( serializedDirectory  );
        } finally {
            pm.close();
        }
        
    }

    private byte[] serialize( final RAMDirectory directory ) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final CBZip2OutputStream bzip2OutputStream = new CBZip2OutputStream( byteArrayOutputStream );
        final ObjectOutputStream stream = new ObjectOutputStream( bzip2OutputStream );
        
        stream.writeObject( directory );
        
        stream.close();
        
        return byteArrayOutputStream.toByteArray();
    }


    private org.apache.lucene.document.Document convert( final Element docElement ) {
        final org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
        
        final Map<String,String> fieldValues = convertToMap( docElement ); 
        
        
        addStoredFields( fieldValues, doc );
        
        addSpacialFields( fieldValues, doc );
        
        addTypeField( doc );
        
        return doc;
        
    }

    @SuppressWarnings( "unchecked" )
    private Map<String, String> convertToMap( final Element docElement ) {
        final Map<String,String> retval = new HashMap<String, String>();
        for ( final Element field : (List<Element>)docElement.getChildren("field") ) {
            retval.put( field.getAttributeValue( "name" ), field.getText() );
        }
        
        return retval;
    }

    private void addTypeField( final org.apache.lucene.document.Document doc ) {
        doc.add( new Field( "type", "PUBLIC_TRANSPORT", Store.YES, Index.NOT_ANALYZED_NO_NORMS ) );
    }

    private void addSpacialFields( final Map<String, String> fieldValues, final org.apache.lucene.document.Document doc ) {
        doc.add(new Field( IndexFieldNames.LAT_SEARCH, NumericUtils.doubleToPrefixCoded(Double.parseDouble( fieldValues.get( "lat" ) )),Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS ));
        doc.add(new Field( IndexFieldNames.LNG_SEARCH, NumericUtils.doubleToPrefixCoded(Double.parseDouble( fieldValues.get( "lng" ) )),Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS ));
    }

    private void addStoredFields( final Map<String, String> fieldValues, final org.apache.lucene.document.Document doc ) {
        extractFieldToDocument( fieldValues, doc, IndexFieldNames.ID );
        extractFieldToDocument( fieldValues, doc, IndexFieldNames.LAT );
        extractFieldToDocument( fieldValues, doc, IndexFieldNames.LNG );
        extractFieldToDocument( fieldValues, doc, IndexFieldNames.STATION_TYPE );
        extractFieldToDocument( fieldValues, doc, IndexFieldNames.STATION_NAME );
        extractFieldToDocument( fieldValues, doc, IndexFieldNames.OPERATOR );
    }

    private void extractFieldToDocument( final Map<String, String> fieldValues, final org.apache.lucene.document.Document doc, final String field ) {
        final String value = fieldValues.get( field );
        doc.add( new Field( field, value == null ? "" : value, Store.YES, Index.NO ) );
    }

    private Element getRootElement( final InputStream inStream ) throws JDOMException, IOException {
        final SAXBuilder builder = new SAXBuilder();
        
        builder.setValidation( false );
        
        final Document doc = builder.build( new InputStreamReader( inStream, Charset.forName( "utf8" ) ) );

        final Element root = doc.getRootElement();
        
        return root;
    }
 
    
    
}
