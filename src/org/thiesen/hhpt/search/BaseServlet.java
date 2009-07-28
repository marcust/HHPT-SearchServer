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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;

import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SingleInstanceLockFactory;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.thiesen.hhpt.search.beans.SerializedRamDirectory;
import org.thiesen.hhpt.search.service.PMF;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


public abstract class BaseServlet extends HttpServlet {

    private static final long serialVersionUID = -3521617021380761645L;

    protected RAMDirectory loadDirectory() throws IOException, ClassNotFoundException {
        final PersistenceManager pm = PMF.get().getPersistenceManager();

        final Key indexKey = makeIndexKey();
        try {
            final SerializedRamDirectory e = pm.getObjectById( SerializedRamDirectory.class, indexKey );

            final byte[] serializedDirectory = e.getSerializedRamDirectory().getBytes();

            final ByteArrayInputStream inputStream = new ByteArrayInputStream( serializedDirectory );

            final CBZip2InputStream bzip2inputStream = new CBZip2InputStream( inputStream );
            
            final ObjectInputStream ios = new ObjectInputStream( bzip2inputStream );

            final RAMDirectory directory = (RAMDirectory)ios.readObject();
            
            directory.setLockFactory( new SingleInstanceLockFactory() );
            
            return directory;

        } catch ( final JDOObjectNotFoundException e ) {
            return new RAMDirectory();
        }

        

    }

    protected Key makeIndexKey() {
        final Key k = KeyFactory.createKey(SerializedRamDirectory.class.getSimpleName(), "INDEX");
        return k;
    }


}
