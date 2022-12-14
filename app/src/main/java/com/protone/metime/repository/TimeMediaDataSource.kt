package com.protone.metime.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.protone.common.baseType.withDefaultContext
import com.protone.common.entity.GalleryMedia
import com.protone.component.database.dao.DatabaseBridge
import java.lang.NullPointerException
import java.time.Year
import java.util.*

class TimeMediaDataSource :
    PagingSource<Calendar, GalleryMedia>() {

    override fun getRefreshKey(state: PagingState<Calendar, GalleryMedia>): Calendar? {
        return null
    }

    override suspend fun load(params: LoadParams<Calendar>): LoadResult<Calendar, GalleryMedia> =
        withDefaultContext {
            (params.key ?: Calendar.getInstance()
                .apply { timeInMillis = System.currentTimeMillis() })
                .let { calendar ->
                    val currentTime = calendar.timeInMillis / 1000
                    calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1)
                    DatabaseBridge.instance
                        .galleryDAOBridge
                        .getAllMediaBetweenDate(currentTime, calendar.timeInMillis / 1000)?.let re@{
                            if (it.isEmpty()) return@re null
                            it as MutableList<GalleryMedia>
                            val firstRandom = (0..it.size).random()
                            val firstMedia = it[firstRandom]
                            it.remove(firstMedia)
                            LoadResult.Page(
                                listOf(firstMedia, it[(0..it.size).random()]),
                                null,
                                calendar
                            )
                        } ?: LoadResult.Error(NullPointerException("No Media"))
                }
        }

}