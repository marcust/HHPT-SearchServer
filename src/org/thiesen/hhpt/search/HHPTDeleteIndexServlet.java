package org.thiesen.hhpt.search;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thiesen.hhpt.search.beans.SerializedRamDirectory;
import org.thiesen.hhpt.search.service.PMF;

import com.google.appengine.api.datastore.Key;

public class HHPTDeleteIndexServlet extends BaseServlet {

    private static final long serialVersionUID = 7936855986839996233L;

    @Override
    public void doGet( @SuppressWarnings( "unused" ) final HttpServletRequest req, final HttpServletResponse resp ) throws IOException {
    final PersistenceManager pm = PMF.get().getPersistenceManager();
        
        final Key indexKey = makeIndexKey();
        final SerializedRamDirectory e = pm.getObjectById( SerializedRamDirectory.class, indexKey );
        
        
        pm.deletePersistent( e );
        
        resp.setContentType( "text/plain" );
        resp.getWriter().println("OK");
        


    }


}
