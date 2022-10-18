package com.bupt.deck.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WSConnEventDao {
    @Insert
    void insert(WSConnEvent wsConnEvent);

    @Query("Select * from WSConnEvent")
    List<WSConnEvent> getAll();

    /**
     * Get entry with ID larger than {offset} and return max number of {limit} entries
     *
     * @param offset ID offset in database
     * @param limit  max number to get entries in database
     * @return A list which contains WSConnChecking entries to be sent to gateway.
     */
    @Query("Select * from WSConnEvent where id > (:offset) limit (:limit)")
    List<WSConnEvent> getIDLargerThanWithLimit(int offset, int limit);
}
