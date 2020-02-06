package com.mediapocket.android.details.mapper

import android.content.Context
import com.mediapocket.android.R
import com.mediapocket.android.core.download.model.DownloadError
import com.mediapocket.android.mapper.Mapper

class DownloadErrorToStringMapper(private val context: Context): Mapper<DownloadError, String> {

    override fun map(item: DownloadError): String {
        return when(item) {
            DownloadError.WRITE_FILE_ERROR -> context.getString(R.string.error_write_file_error)
            DownloadError.NO_NETWORK_CONNECTION -> context.getString(R.string.error_no_network_connection)
            else -> context.getString(R.string.error_common_network_error)
        }
    }

}