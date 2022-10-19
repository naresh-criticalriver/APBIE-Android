package com.application.subhamastu

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE: Int = 1
    private lateinit var webChromeViewClient: WebChromeClient
    private lateinit var myWebView: WebView
    private lateinit var progressBar: ProgressBar
    private var mUploadCallbackAboveL: ValueCallback<Array<Uri>>? = null
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = resources.getColor(R.color.white, null)
        WindowCompat.getInsetsController(
            window,
            window.decorView
        )?.isAppearanceLightStatusBars = true
        window.decorView.setBackgroundColor(
            resources.getColor(
                R.color.white,
                null
            )
        )
        setContentView(R.layout.activity_main)
        // webview definition
        myWebView = findViewById<WebView>(R.id.webview)
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
//        myWebView.webChromeClient = initChromeWebViewClient()
        myWebView.webViewClient = initWebViewClient()
//        myWebView.webViewClient = Client()
        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.domStorageEnabled = true
        myWebView.settings.allowFileAccess = true
        myWebView.settings.allowContentAccess = true
        // myWebView.settings.supportZoom()
        // myWebView.visibility = View.INVISIBLE
        myWebView.settings.userAgentString = "android"
        myWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myWebView.settings.mixedContentMode = 0
        }
        myWebView.loadUrl("https://bie.ap.gov.in/")
    }


    private fun initChromeWebViewClient(): WebChromeClient {
        webChromeViewClient = object : WebChromeClient() {
            /**
             * API > = 21 (Android 5.0.1) calls back this method
             */
            override fun onShowFileChooser(
                webView: WebView?,
                valueCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                //(1) when the method calls back, it indicates that the version API > = 21. In this case, assign the result to muploadcallbackabovel to make it! = null
                mUploadCallbackAboveL = valueCallback
                takePhoto()
                return true
            }
        }
        return webChromeViewClient
    }

    private fun initWebViewClient(): WebViewClient {
        return object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }
    }

    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE) {

            val uri = intent.data
            var bitmap: Bitmap? = null
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val file: File = File(imageUri!!.toString())
            val file_size = (file.length() / 1024).toString().toInt()
            //After the above two assignment operations (1) and (2), we can decide which processing method to adopt according to whether its value is empty

            if (mUploadCallbackAboveL != null) {
                chooseAbove(resultCode, data);
            } else {
                Toast.makeText(this, "an error occurred.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private fun takePhoto() {
        //Adjust the camera in a way that specifies the storage location for taking pictures
        var photoFile: File? = null
        val authorities: String = applicationContext.packageName + ".provider"
        try {
            photoFile = createImageFile()
            imageUri = FileProvider.getUriForFile(this, authorities, photoFile)
            Log.e("TAG", "URI---" + imageUri.toString())
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        val Photo = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val chooserIntent = Intent.createChooser(Photo, "Image Chooser")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf<Parcelable>(captureIntent))
        startActivityForResult(chooserIntent, REQUEST_CODE)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val imageFileName = "JPEG_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return File(applicationContext.cacheDir.toString() + File.separator + imageFileName + ".jpg")
    }

    private fun chooseAbove(resultCode: Int, data: Intent?) {
        if (RESULT_OK == resultCode) {
            //updatePhotos()

            if (data != null) {
                //Here is the processing of selecting pictures from a file
                val results: Array<Uri>
                val uriData: Uri? = data.data
                if (uriData != null) {
                    results = arrayOf(uriData)
                    mUploadCallbackAboveL!!.onReceiveValue(results)
                } else {
                    results = imageUri?.let { arrayOf(it) }!!
                    mUploadCallbackAboveL!!.onReceiveValue(results)
                }
            } else {
                if (imageUri != null) {
                    mUploadCallbackAboveL!!.onReceiveValue(arrayOf(imageUri!!))
                }
            }
        } else {
            mUploadCallbackAboveL!!.onReceiveValue(null)
        }
        mUploadCallbackAboveL = null
    }

    inner class Client : WebViewClient() {

        // If you will not use this method url links are open in new browser
        // not in webview
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: WebResourceRequest
        ): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.loadUrl(request.url.toString())
            }
            return true
        }

        override fun onReceivedError(
            view: WebView?,
            errorCode: Int,
            description: String?,
            failingUrl: String?
        ) {
            super.onReceivedError(view, errorCode, description, failingUrl)
            Log.e("TAG", "onReceivedError ")
        }

        // Show loader on url load
        override fun onLoadResource(view: WebView, url: String) {
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            //progressBar.visibility = View.GONE
        }

        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.e("TAG", "onReceivedHttpError ${errorResponse?.statusCode}")
            }
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            super.onReceivedError(view, request, error)
            Log.e("TAG", "onReceivedError ")
            WebViewClient.ERROR_AUTHENTICATION
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.e(
                    "TAG",
                    "error code: ${error.errorCode} " + request.url.toString() + " , " + error.description
                )
            }
        }

        override fun onReceivedSslError(
            view: WebView,
            handler: SslErrorHandler,
            error: SslError
        ) {
            super.onReceivedSslError(view, handler, error)
            Log.e("TAG", "SSl error ")
        }


    }


    private fun requestImgPermissions() {
        if (Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                1
            )
        }

    }

}