package be.hogent.kolveniershof.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import be.hogent.kolveniershof.api.KolvApi
import be.hogent.kolveniershof.database.DAO.*
import be.hogent.kolveniershof.database.KolveniershofDatabase
import be.hogent.kolveniershof.database.databaseModels.DatabaseLunchUnit
import be.hogent.kolveniershof.database.databaseModels.DatabaseUser
import be.hogent.kolveniershof.database.databaseModels.DatabaseWorkday
import be.hogent.kolveniershof.domain.Comment
import be.hogent.kolveniershof.domain.Workday
import be.hogent.kolveniershof.network.NetworkUser
import be.hogent.kolveniershof.network.NetworkWorkday
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class WorkdayRepository(private val kolvApi: KolvApi, val workdayDao: WorkdayDao, val workdayUserJOINDao: WorkdayUserJOINDao, val busRepository: BusRepository, val activityRepository: ActivityRepository, val lunchUnitDao: LunchUnitDao, val c: Context) : BaseRepo(c) {



    fun getWorkdays(authToken:String): LiveData<MutableList<Workday>> {
        checkDatabaseWorkdays(authToken)
        return Transformations.map(
            workdayDao.getAllWorkdays(),
            {list -> list.map { l -> databaseWorkdayToWorkday(l) }.toMutableList()}
        )
    }

    fun getWorkdayById(authToken: String,id: String): LiveData<Workday> {
        checkDatabaseWorkdays(authToken)
        return Transformations.map(workdayDao.getWorkdayById(id), {dbWorkday -> databaseWorkdayToWorkday(dbWorkday)})
    }

    fun getWorkdayByDateByUser(authToken: String, date: String, userId: String):LiveData<Workday>{
        checkDatabaseWorkdays(authToken)
        return Transformations.map(workdayDao.getByWorkdateByDate(date)) { dbWorkday : DatabaseWorkday? -> run {
            if (dbWorkday == null) {
                null
            }else {
                databaseWorkdayToWorkday(dbWorkday)
            }
        }}
    }

    private fun checkDatabaseWorkdays(authToken: String){
        //Check if empty, if true --> check if connected and get directly from API
        if( workdayDao.getRowCount() <= 0 && isConnected()){
            val workdaysList = kolvApi.getWorkdays(authToken).subscribeOn(Schedulers.io()).blockingSingle()
           workdaysList.forEach { wd -> saveWorkdayToDatabase(wd) }
        }
    }

    private fun databaseWorkdayToWorkday(dbWorkday : DatabaseWorkday) : Workday {
        val daycareMentors =  workdayUserJOINDao.getUsersFromWorkday(dbWorkday.id).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).blockingGet().map { user -> DatabaseUser.toUser(user) }.toMutableList()
        val morningBusses = busRepository.getBusUnitFromWorkday(dbWorkday.id, false)
        val eveningBusses = busRepository.getBusUnitFromWorkday(dbWorkday.id, true)
        val amActivities = activityRepository.getAmActivitiesFromWorkday(dbWorkday.id)
        val pmActivities = activityRepository.getPmActivitiesFromWorkday(dbWorkday.id)
        val dayActivities = activityRepository.getDayActivitiesFromWorkday(dbWorkday.id)

        return Workday(
            id = dbWorkday.id,
            date = Date(dbWorkday.date),
            daycareMentors = daycareMentors,
            morningBusses = morningBusses,
            eveningBusses = eveningBusses,
            amActivities = amActivities,
            lunch = DatabaseLunchUnit.databaseLunchUnitToLunchUnit(lunchUnitDao.getLunchFromWorkday(dbWorkday.id).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).blockingGet()),
            pmActivities = pmActivities,
            isHoliday = dbWorkday.isHoliday,
            comments = mutableListOf<Comment>(),
            dayActivities = dayActivities

            )
    }

    private fun saveWorkdayToDatabase(workday : Workday) : DatabaseWorkday {

        val dbWorkday =  DatabaseWorkday(
            id = workday.id,
            date = workday.date.toString(),
            isHoliday = workday.isHoliday
        )

        workdayDao.insertItem(dbWorkday)

        activityRepository.addAmActivities(workday.amActivities, workday.id)
        activityRepository.addPmActivities(workday.pmActivities,workday.id)
        activityRepository.addDayActivities(workday.dayActivities,workday.id)
        busRepository.addBusses(workday.morningBusses,workday.id)
        busRepository.addBusses(workday.eveningBusses,workday.id)




        return dbWorkday
    }
}