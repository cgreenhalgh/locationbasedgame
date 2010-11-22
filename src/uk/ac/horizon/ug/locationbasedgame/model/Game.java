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

/** a single (multi-player) game instance.
 * 
 * @author cmg
 *
 */
@Entity
public class Game {
	/** key - autogenerated */
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Key key; 
    /** game configuration (key) */
    private Key gameConfigurationKey;
    /** title */
    private String title;
    /** tag */
    private String tag;
    /** status */
    private GameStatus status;
    /** creation date/time */
    private long createdTime;
    
    /** cons */
    public Game() {
    	
    }
    /** generate key */
    public static final Key idToKey(String id) {
    	return KeyFactory.createKey(Game.class.getSimpleName(), id);
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
	 * @return the id (string of key)
	 */
	public String getId() {
		if (key!=null)
			return key.getName();
		else
			return null;
		//return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		if (id==null)
			key = null;
		else
			key = idToKey(id);
		//this.id = id;
	}
	/**
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}
	/**
	 * @param tag the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}
	/**
	 * @return the gameConfigurationKey
	 */
	public Key getGameConfigurationKey() {
		return gameConfigurationKey;
	}
	/**
	 * @param gameConfigurationKey the gameConfigurationKey to set
	 */
	public void setGameConfigurationKey(Key gameConfigurationKey) {
		this.gameConfigurationKey = gameConfigurationKey;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the status
	 */
	public GameStatus getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(GameStatus status) {
		this.status = status;
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
    
}
