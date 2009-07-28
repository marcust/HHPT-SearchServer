package org.thiesen.hhpt.search;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;

public class HHPTCountIndexServlet extends BaseServlet {
   
    private static final long serialVersionUID = 7936855986839996233L;

    @Override
    public void doGet( @SuppressWarnings( "unused" ) final HttpServletRequest req, final HttpServletResponse resp ) throws IOException, ServletException {
        RAMDirectory dir;
        try {
            dir = loadDirectory();
            final IndexSearcher searcher = new IndexSearcher( dir, false );


            resp.setContentType( "text/plain" );
            resp.getWriter().println( searcher.maxDoc() );

        } catch ( final ClassNotFoundException e ) {
            throw new ServletException( e );
        }
        
    }


}
