package be.hogent.kolveniershof.database.DAO

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import be.hogent.kolveniershof.database.databaseModels.DatabaseActivityUnit
import be.hogent.kolveniershof.database.databaseModels.DatabaseUser

@Dao
interface ActivityUnitUserJOINDao {

    @Query("""
        SELECT * FROM user_table 
        INNER JOIN activityUnitUserJoin 
        ON user_table.user_id = activityUnitUserJoin.userIdJOIN 
        WHERE activityUnitUserJoin.activityUnitIdJOIN =:activityUnit_Id
        """)
    fun getUserFromActivities(activityUnit_Id: String): LiveData<MutableList<DatabaseUser>>

    @Query("""
        SELECT * FROM activityUnit_table 
        INNER JOIN activityUnitUserJoin 
        ON activityUnit_table.activityUnit_id = activityUnitUserJoin.activityUnitIdJOIN 
        WHERE activityUnitUserJoin.userIdJOIN =:user_id
        """)
    fun getActivitiesFromUsers(user_id: String): LiveData<MutableList<DatabaseActivityUnit>>
}