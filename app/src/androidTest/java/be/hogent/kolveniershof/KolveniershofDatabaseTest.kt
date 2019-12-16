package be.hogent.kolveniershof

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import be.hogent.kolveniershof.database.*
import be.hogent.kolveniershof.database.DAO.*
import be.hogent.kolveniershof.database.databaseModels.DatabaseUser
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class KolveniershofDatabaseTest {
    private lateinit var db: KolveniershofDatabase
    private lateinit var userDao: UserDao
    private lateinit var activityUnitDAO: ActivityUnitDao
    private lateinit var activityUnitUserJOINDao: ActivityUnitUserJOINDao
    private lateinit var workdayDao: WorkdayDao


    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, KolveniershofDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        userDao = db.UserDao()
        activityUnitUserJOINDao = db.ActivityUnitUserJOINDao()
        activityUnitDAO = db.ActivityUnitDao()
        workdayDao = db.WorkdayDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    //USER TESTS
    @Test
    @Throws(Exception::class)
    fun insertAndGetUser() {
        val user = DatabaseUser()
        userDao.insertItem(user)
        val dbuser = userDao.getAllUsers().value!!.last()
        Assert.assertEquals(user.userId, dbuser.userId)
    }

    @Test
    @Throws(Exception::class)
    fun insertManyUsers() {
        val user1 = DatabaseUser()
        val user2 = DatabaseUser()
        userDao.insertItem(user1)
        userDao.insertItem(user2)
        val dbusers = userDao.getAllUsers().value!!
        Assert.assertEquals(dbusers.size, 2)
    }

    @Test
    @Throws(Exception::class)
    fun deleteUser() {
        val user1 = DatabaseUser()
        val user2 = DatabaseUser()
        userDao.insertItem(user1)
        userDao.insertItem(user2)
        userDao.delete(user1)
        val dbusers = userDao.getAllUsers().value!!
        Assert.assertEquals(dbusers.size, 1)
    }


}