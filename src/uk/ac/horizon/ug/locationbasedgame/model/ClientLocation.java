/**
 * Copyright 2010 The University of Nottingham
 * 
 * This file is part of locationbasedgame.
 *
 *  locationbasedgame is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  locationbasedgame is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with locationbasedgame.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package uk.ac.horizon.ug.locationbasedgame.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/** (e.g.) a record of a GameClient's location
 * 
 * @author cmg
 *
 */
@Entity
public class ClientLocation {
	/** key - autogenerated */
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Key key; 
    /** game client */
    private Key gameClientKey;
    /** game */
    private Key gameKey;
    /** creation date/time */
    private long createdTime;
    /** 'current' */
    private boolean current;
    /** latitude, degrees WGS84, E6 */
    private int latitudeE6;
    /** longitude, degrees WGS84, E6 */
    private int longitudeE6;
    /** altitude (m) */
    private float altitudeMetres;
    /** accuracy radius (m), optional */
    private float radiusMetres; 
    
    /** cons */
    public ClientLocation() {
    	
    }
    /** generate key */
    public static final Key makeKey(GameClient gameClient, long createdTime) {
    	return KeyFactory.createKey(ClientLocation.class.getSimpleName(), gameClient.getKey().getName()+":"+createdTime);
    }

	/**
	 * @return the key
	 */
	public Key getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(Key key) {
		this.key = key;
	}
	/**
	 * @return the gameClientKey
	 */
	public Key getGameClientKey() {
		return gameClientKey;
	}
	/**
	 * @param gameClientKey the gameClientKey to set
	 */
	public void setGameClientKey(Key gameClientKey) {
		this.gameClientKey = gameClientKey;
	}
	/**
	 * @return the gameKey
	 */
	public Key getGameKey() {
		return gameKey;
	}
	/**
	 * @param gameKey the gameKey to set
	 */
	public void setGameKey(Key gameKey) {
		this.gameKey = gameKey;
	}
	/**
	 * @return the createdTime
	 */
	public long getCreatedTime() {
		return createdTime;
	}
	/**
	 * @param createdTime the createdTime to set
	 */
	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}
	/**
	 * @return the current
	 */
	public boolean isCurrent() {
		return current;
	}
	/**
	 * @param current the current to set
	 */
	public void setCurrent(boolean current) {
		this.current = current;
	}
	/**
	 * @return the latitudeE6
	 */
	public int getLatitudeE6() {
		return latitudeE6;
	}
	/**
	 * @param latitudeE6 the latitudeE6 to set
	 */
	public void setLatitudeE6(int latitudeE6) {
		this.latitudeE6 = latitudeE6;
	}
	/**
	 * @return the longitudeE6
	 */
	public int getLongitudeE6() {
		return longitudeE6;
	}
	/**
	 * @param longitudeE6 the longitudeE6 to set
	 */
	public void setLongitudeE6(int longitudeE6) {
		this.longitudeE6 = longitudeE6;
	}
	/**
	 * @return the altitudeMetres
	 */
	public float getAltitudeMetres() {
		return altitudeMetres;
	}
	/**
	 * @param altitudeMetres the altitudeMetres to set
	 */
	public void setAltitudeMetres(float altitudeMetres) {
		this.altitudeMetres = altitudeMetres;
	}
	/**
	 * @return the radiusMetres
	 */
	public float getRadiusMetres() {
		return radiusMetres;
	}
	/**
	 * @param radiusMetres the radiusMetres to set
	 */
	public void setRadiusMetres(float radiusMetres) {
		this.radiusMetres = radiusMetres;
	}
}
