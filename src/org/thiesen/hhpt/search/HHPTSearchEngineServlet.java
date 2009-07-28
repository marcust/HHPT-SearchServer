package org.thiesen.hhpt.search;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.spatial.tier.LatLongDistanceFilter;
import org.thiesen.hhpt.search.beans.SearchParameters;
import org.thiesen.hhpt.shared.model.station.Station;
import org.thiesen.hhpt.shared.model.station.StationType;
import org.thiesen.hhpt.shared.model.station.Stations;

public class HHPTSearchEngineServlet extends BaseServlet {
   
    private static final long serialVersionUID = 7936855986839996233L;

    private static final int JSON_PADDING_WIDTH = 10;

    private volatile Searcher _cachedSearcher;
    
    @Override
    public void doGet( final HttpServletRequest req, final HttpServletResponse resp ) throws IOException, ServletException {
        try {
            final Stations stations = findStations( req );
            
            writeStations( resp, stations );
        } catch ( final ClassNotFoundException e ) {
            throw new ServletException( e );
        }
        
        
    }

    private void writeStations( final HttpServletResponse resp, final Stations stations ) throws IOException, JSONException {
        final JSONArray array = new JSONArray();
        
        for ( final Station s : stations ) {
            array.add( toJSONObject( s ) );
        }
        
        writeJSON( resp, new JSONObject().element( "results", array ) );
    }

    private JSONObject toJSONObject( final Station s ) {
        final JSONObject obj = new JSONObject();
        
        obj.element( IndexFieldNames.ID, s.getId().stringValue() );
        obj.element( IndexFieldNames.LAT, s.getPosition().getLatitude() );
        obj.element( IndexFieldNames.LNG, s.getPosition().getLongitude() );
        obj.element( IndexFieldNames.STATION_NAME, s.getName() );
        obj.element( IndexFieldNames.OPERATOR, s.getOperator().stringValue() );
        obj.element( IndexFieldNames.STATION_TYPE, s.getType() );
        
        return obj;
        
    }

    private void writeJSON( final HttpServletResponse response, final JSONObject jsonObj ) throws IOException {
        response.setContentType( "text/json;charset=UTF-8" );

        final PrintWriter writer = response.getWriter();

        writer.print( jsonObj.toString( JSON_PADDING_WIDTH ) );

        writer.flush();
        writer.close();

        
    }

    private Stations findStations( final HttpServletRequest req ) throws IOException, ClassNotFoundException {
        final SearchParameters params = extractSearchParameters( req );
        
        return execute( params );
    }

    private Stations execute( final SearchParameters params ) throws IOException, ClassNotFoundException {
        final Searcher searcher = getSearcher();
        
        final LatLongDistanceFilter filter = new LatLongDistanceFilter( params.getLatitude(), 
                                                                        params.getLongitude(), 
                                                                        params.getRadiusInMiles(), 
                                                                        IndexFieldNames.LAT_SEARCH,
                                                                        IndexFieldNames.LNG_SEARCH
                                                                         );
        

        
        final TopDocs results = searcher.search( new MatchAllDocsQuery(), filter, 100 );
        
        return extractStations( results, searcher );
        
        
    }

    private Stations extractStations( final TopDocs search, final Searcher searcher ) throws CorruptIndexException, IOException {
        final Stations retval = new Stations();
        for ( final ScoreDoc doc : search.scoreDocs ) {
            final Document document = searcher.doc( doc.doc );
            retval.add( document.get( IndexFieldNames.ID ),
                        document.get( IndexFieldNames.LAT ),
                        document.get( IndexFieldNames.LNG ),
                        StationType.valueOf( document.get( IndexFieldNames.STATION_TYPE ) ),
                        document.get( IndexFieldNames.STATION_NAME ),
                        document.get( IndexFieldNames.OPERATOR ) );
        }
        
        return retval;
    }

    private Searcher getSearcher() throws CorruptIndexException, IOException, ClassNotFoundException {
        Searcher searcher = _cachedSearcher;
        if ( searcher != null ) {
            return searcher;
        }
        searcher = new IndexSearcher( loadDirectory(), true );
        
        _cachedSearcher = searcher;
        
        return searcher;
    }

    private SearchParameters extractSearchParameters( final HttpServletRequest req ) {

        return SearchParameters.valueOf( extractDouble( req.getParameter( "lat" ) ), extractDouble( req.getParameter( "lng" ) ),
                                         extractDouble( req.getParameter( "radius" ) ) );
    }

    private double extractDouble( final String parameter ) {
        return "".equals( parameter ) || parameter == null ? 0.0D : Double.parseDouble( parameter );
    }
}
