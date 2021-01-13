package cc.music.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class Track(
    val upload_name: String,
    val files: List<CFile>,
    val user_name: String,
    val image: String
) : Serializable {
    val duration: String
        get() = try {
            files.last { !it.file_format_info.ps.isNullOrEmpty() }.file_format_info.ps!!
        } catch (e: NoSuchElementException) {
            "NaN"
        }
    val audio: String
        get() = files.last { !it.download_url.endsWith(".zip") }.download_url
}
