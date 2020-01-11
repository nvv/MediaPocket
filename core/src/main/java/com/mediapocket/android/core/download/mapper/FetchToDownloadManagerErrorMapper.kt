package com.mediapocket.android.core.download.mapper

import com.mediapocket.android.core.download.model.DownloadError
import com.mediapocket.android.mapper.Mapper
import com.tonyodev.fetch2.Error

class FetchToDownloadManagerErrorMapper : Mapper<Error, DownloadError> {

    override fun map(item: Error): DownloadError {
        return when(item) {
            Error.FILE_NOT_CREATED, Error.WRITE_PERMISSION_DENIED, Error.NO_STORAGE_SPACE -> DownloadError.WRITE_FILE_ERROR
            Error.NO_NETWORK_CONNECTION -> DownloadError.NO_NETWORK_CONNECTION
            else -> DownloadError.COMMON_NETWORK_ERROR
        }
    }

}