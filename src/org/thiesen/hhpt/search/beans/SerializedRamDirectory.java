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
package org.thiesen.hhpt.search.beans;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class SerializedRamDirectory {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key _id;

    @Persistent
    private Blob _serializedRamDirectory;
    
     public SerializedRamDirectory( final Key k, final byte[] bytes ) {
         setId( k );
         _serializedRamDirectory = new Blob( bytes );
     }

    
    public void setSerializedRamDirectory( final Blob serializedRamDirectory ) {
        _serializedRamDirectory = serializedRamDirectory;
    }

    public Blob getSerializedRamDirectory() {
        return _serializedRamDirectory;
    }


    public void setId( final Key id ) {
        _id = id;
    }


    public Key getId() {
        return _id;
    } 

}
