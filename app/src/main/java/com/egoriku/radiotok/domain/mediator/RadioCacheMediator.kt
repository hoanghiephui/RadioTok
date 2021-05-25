package com.egoriku.radiotok.domain.mediator

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import com.egoriku.radiotok.common.ext.logD
import com.egoriku.radiotok.common.model.RadioItemModel
import com.egoriku.radiotok.domain.repository.IRadioFetchNetworkRepository
import com.egoriku.radiotok.radioplayer.data.CurrentRadioQueueHolder
import com.egoriku.radiotok.radioplayer.data.mediator.IRadioCacheMediator
import com.egoriku.radiotok.radioplayer.ext.createPlayableMediaItem
import com.egoriku.radiotok.radioplayer.model.MediaPath
import com.egoriku.radiotok.radioplayer.repository.IMediaItemRepository

class RadioCacheMediator(
    private val radioFetchNetworkRepository: IRadioFetchNetworkRepository,
    private val mediaItemRepository: IMediaItemRepository,
    private val currentRadioQueueHolder: CurrentRadioQueueHolder
) : IRadioCacheMediator {

    private var _radios = emptyList<RadioItemModel>()

    override suspend fun loadNextRadio() {
        logD("loadNextRadio")
        checkCacheOrLoad()

        val items = getMediaMetadataBy(mediaPath = currentRadioQueueHolder.currentPath)

        currentRadioQueueHolder.set(items)
    }

    override suspend fun switchToRandomRadios() {
        logD("switchToRandomRadios")

        checkCacheOrLoad()

        currentRadioQueueHolder.currentPath = MediaPath.RandomRadio
        loadInitial()
    }

    override suspend fun switchToLikedRadios() {
        logD("switchToLikedRadios")

        checkCacheOrLoad()

        currentRadioQueueHolder.currentPath = MediaPath.LikedRadio
        loadInitial()
    }

    override suspend fun getMediaBrowserItemsBy(mediaPath: MediaPath): List<MediaBrowserCompat.MediaItem> {
        checkCacheOrLoad()

        return when (mediaPath) {
            is MediaPath.Root -> mediaItemRepository.getRootItems()
            is MediaPath.RandomRadio -> {
                currentRadioQueueHolder.currentPath = mediaPath

                val mediaMetadata = getMediaMetadataBy(mediaPath)

                currentRadioQueueHolder.set(mediaMetadata)

                listOf(createPlayableMediaItem(mediaMetadata))
            }
            else -> throw UnsupportedOperationException()
        }
    }

    override suspend fun getMediaMetadataBy(mediaPath: MediaPath): MediaMetadataCompat {
        return when (mediaPath) {
            is MediaPath.RandomRadio -> mediaItemRepository.getRandomItems(_radios)
            is MediaPath.LikedRadio -> throw IllegalArgumentException()
            else -> throw IllegalArgumentException()
        }
    }

    private suspend fun checkCacheOrLoad() {
        // TODO: 2.05.21 Add DB check

        if (_radios.isEmpty()) {
            _radios = radioFetchNetworkRepository.load()
        }
    }

    private suspend fun loadInitial() {
        val items = getMediaMetadataBy(mediaPath = currentRadioQueueHolder.currentPath)

        currentRadioQueueHolder.set(items)
    }
}