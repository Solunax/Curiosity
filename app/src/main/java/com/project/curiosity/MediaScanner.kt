import android.content.Context
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import java.io.File

class MediaScanner(context: Context?, f: File) : MediaScannerConnectionClient {
    private val _msc: MediaScannerConnection
    private val _file: File

    override fun onMediaScannerConnected() {
        _msc.scanFile(_file.absolutePath, null)
    }

    override fun onScanCompleted(path: String?, uri: Uri?) {
        _msc.disconnect()
    }

    init {
        _file = f
        _msc = MediaScannerConnection(context, this)
        _msc.connect()
    }
}