package cc.music.model

import androidx.annotation.Keep

@Keep
data class CFile(val download_url: String, val file_format_info: FileFormatInfo)
