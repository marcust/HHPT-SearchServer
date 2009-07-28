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

import org.thiesen.hhpt.shared.model.position.Latitude;
import org.thiesen.hhpt.shared.model.position.Longitude;


public class SearchParameters {

    private final Latitude _latitude;
    private final Longitude _longitude;
    private final double _radius;

    public SearchParameters( final Latitude lat, final Longitude lng, final double radius ) {
        _latitude = lat;
        _longitude = lng;
        _radius = radius;
        
    }

    public static SearchParameters valueOf( final double latitude, final double longitude, final double radius ) {
        return new SearchParameters( Latitude.valueOf( latitude ), Longitude.valueOf( longitude ), radius );
                                    
        
    }

    public double getLatitude() {
        return _latitude.doubleValue();
    }

    public double getLongitude() {
        return _longitude.doubleValue();
        
    }

    public double getRadiusInMiles() {
        return _radius;
        
    }

}
