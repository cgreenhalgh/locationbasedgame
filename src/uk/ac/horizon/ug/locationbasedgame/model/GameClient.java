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

/** A single client's relationship to a Game, which may span multiple ClientConversations (due to recovery).
 * 
 * @author cmg
 *
 */
@Entity
public class GameClient {
	/** key - autogenerated; based on clientId and gameId */
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Key key; 
    /** current conversation Id */
    private String currentConverationId;
    /** version */
    private Key gameKey;
    /** client Id (from client); note that the same client device could concurrently be engaged in multiple games! */
    private String clientId;
    /** client nickname */
    private String nickname;
    /** date/time of creation (login) */
    private long creationTime;
    /** state replicated from GameStatus */
    //private GameStatus gameStatus;
    
    /** cons */
    public GameClient() {
    	
    }
    /** generate key */
    public static final Key makeKey(Game game, String clientId) {
    	return KeyFactory.createKey(GameClient.class.getSimpleName(), game.getId()+":"+clientId);
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
	 * @return the currentConverationId
	 */
	public String getCurrentConverationId() {
		return currentConverationId;
	}
	/**
	 * @param currentConverationId the currentConverationId to set
	 */
	public void setCurrentConverationId(String currentConverationId) {
		this.currentConverationId = currentConverationId;
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
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}
	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	/**
	 * @return the nickname
	 */
	public String getNickname() {
		return nickname;
	}
	/**
	 * @param nickname the nickname to set
	 */
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	/**
	 * @return the creationTime
	 */
	public long getCreationTime() {
		return creationTime;
	}
	/**
	 * @param creationTime the creationTime to set
	 */
	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}
}
