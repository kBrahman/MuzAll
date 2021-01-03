package cc.music.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class CFile(val download_url: String, val file_format_info: FileFormatInfo) : Serializable
