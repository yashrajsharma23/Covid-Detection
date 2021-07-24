package com.matrix.aimlcapstone

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.matrix.aimlcapstone.R
import com.matrix.aimlcapstone.databinding.ActivityMainWebviewBinding


class MainWebview : AppCompatActivity(), View.OnClickListener{
    lateinit var binding:ActivityMainWebviewBinding
    lateinit var context: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_webview)
        init()
    }

    fun init(){
        val title = intent.getStringExtra("title")
        if (title!=null && title.length>0){
            binding.tvHeader.setText(title)
        }
        val url = "http://covid-detection.azurewebsites.net/"
        context=this
        System.out.println("URL:::::::::::: 1 " + url)

        binding.webview.visibility=View.GONE
        binding.progressBar.visibility=View.VISIBLE


        binding.webview.getSettings().setJavaScriptEnabled(true)
        binding.webview.getSettings().setLoadWithOverviewMode(true)
        binding.webview.getSettings().setUseWideViewPort(true)
        binding.webview.getSettings().setDomStorageEnabled(true);

//        binding.webview.setWebViewClient(SSLTolerentWebViewClient(context))

        binding.webview.setWebViewClient(object : WebViewClient() {

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                Log.d("Failure Url :", failingUrl!!)
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler,
                error: SslError?
            ) {
                super.onReceivedSslError(view, handler, error);
                handler.proceed() // Ignore SSL certificate errors
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                url: String
            ): Boolean {

                binding.webview.visibility=View.GONE
                binding.progressBar.visibility=View.VISIBLE
                view.loadUrl(url)
                return false
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                println("URL:::::::::::::: 2nd: " + request!!.url!!)//.encodedPath)
                System.out.println("URL:::::::::::: 3 " + url)
                //view.loadUrl(url)
                if(request.url!!.toString().startsWith("mailto")){
                    val selectorIntent = Intent(Intent.ACTION_SENDTO)
                    selectorIntent.data = Uri.parse("mailto:")
                    val emailIntent = Intent(Intent.ACTION_SEND)
                    emailIntent.putExtra(
                        Intent.EXTRA_EMAIL,
                        arrayOf<String>(request!!.url.toString())
                    )
//        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "The subject");
                    //        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "The subject");
                    emailIntent.selector = selectorIntent

                    startActivity(
                        Intent.createChooser(
                            emailIntent,
                            "Send email..."
                        )
                    )
                }else if(request!!.url.toString().startsWith("tel:")){
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse(request!!.url.toString())
                    startActivity(intent)
                }else{
                    return super.shouldOverrideUrlLoading(view, request)
                }
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                binding.webview.visibility=View.VISIBLE
                binding.progressBar.visibility=View.GONE
            }
        })

        binding.webview.loadUrl(url)

    }

    class myWebClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon)
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
            // TODO Auto-generated method stub
            view.loadUrl(url!!)
            return true
        }
    }

    override fun onClick(p0: View?) {
        /*when(p0!!.id){
            R.id.imgBack -> {
                onBackPressed()
            }
        }*/
    }
}
