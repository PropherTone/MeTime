package com.protone.metime.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.protone.common.baseType.withDefaultContext
import com.protone.common.entity.GalleryMedia
import com.protone.component.database.dao.DatabaseBridge
import java.lang.NullPointerException
import java.util.*

class TimeMediaDataSource :
    PagingSource<Calendar, GalleryMedia>() {

    override val keyReuseSupported: Boolean
        get() = true

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
                    galleryDAO().getMediasByDate(currentTime)?.let current@{
                        if (it.isEmpty()) return@current null
                        LoadResult.Page(it, null, calendar)
                    } ?: galleryDAO().getAllMediaBetweenDate(
                        currentTime,
                        calendar.timeInMillis / 1000
                    )?.let random@{
                        if (it.isEmpty()) return@random null
                        it as MutableList<GalleryMedia>
                        val firstRandom = (it.indices).random()
                        val firstMedia = it[firstRandom]
                        it.remove(firstMedia)
                        LoadResult.Page(
                            listOf(firstMedia, it[(it.indices).random()]),
                            null,
                            calendar
                        )
                    } ?: LoadResult.Error(NullPointerException("No Media"))
                }
        }

    private fun galleryDAO() = DatabaseBridge.instance.galleryDAOBridge
}