package otsuka.fumiya.techacademy.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    //requestPermissionsの第二引数用の定数
    private val PERMISSIONS_REQUEST_CODE = 100

    //画像URIを格納しておくListを作成
    private var arr_imageUri = arrayListOf<Uri>()

    //何枚目の画像を表示しているかをカウントするための変数を作成
    private var cnt = 0

    //タイマー用オブジェクトをnullとする
    private var mTimer: Timer? = null

    //UI描画用オブジェクトの生成
    private var mHandler: Handler = Handler()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //Android 6.0以降のみ許可状態の確認を行う
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            //パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                //許可されている場合は、画像の読み込みを行う
                getContentsInfo()

            } else {

                //許可されていない場合は、許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }

            //Android 5系以下の場合、画像の読み込みを行う
        } else {

            getContentsInfo()
        }


        //---------以下ボタン押下時の処理---------------------------------------

        //戻るボタン押下時の処理
        back_button.setOnClickListener {

            putBackButton()
        }

        //進むボタン押下時の処理
        next_button.setOnClickListener {

            putNextButton()
        }

        //再生ボタン押下時の処理
        start_button.setOnClickListener {

            putStartButton()

        }
    }


    //パーミッションの応答値を受け取るためのメソッド
    //許可された場合のみ、画像の読み込みを行う
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {

            PERMISSIONS_REQUEST_CODE ->

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    getContentsInfo()
                }
        }
    }


    //ContentProviderで端末に保存されている画像を取得し、Listに画像のURIを格納するメソッド
    private fun getContentsInfo() {

        //画像情報を取得する
        val resolver = contentResolver

        val cursor = resolver.query(

            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, //外部ストレージを指定
            null, //項目（nullは全項目指定）
            null, //フィルタ条件　なし
            null, //フィルタ用パラメータ
            null //ソート　なし
        )

        //cursor内から画像を取得し配列に格納する
        //cursor内に画像がある場合のみ処理を実行する
        if (cursor == null){

            return
        }

        if (cursor.moveToFirst()) {

            do {
                //cursorの現在のindexを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)

                //indexから画像のIDを取得する
                val id = cursor.getLong(fieldIndex)

                //画像のIDからURIを取得する
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                //Listに画像URIを格納する
                arr_imageUri.add(imageUri)

                //Logの出力
                Log.d("ANDROID", "URI : " + imageUri.toString())

            } while (cursor.moveToNext())

            //画像の1枚目の設定 cntの初期値は0
            imageView.setImageURI(arr_imageUri[cnt])
        }
        //cursorを閉じる
        cursor.close()

//        //テストログ(cursorを閉じても、配列内の値が消えないことの確認）
//        for (i in arr_imageUri.indices){
//            Log.d("test",arr_imageUri[i].toString())
//        }
    }


    //戻るボタン押下時のメソッド
    private fun putBackButton(){

        Log.d("test1","戻る開始"+cnt.toString())  //************Log

        //画像が存在しない場合、再生中でない場合は処理なしで返却する
        if (arr_imageUri.isEmpty() || mTimer != null){

            return
        }

        //画像URIを格納する配列の最大カウント取得
        val lastCnt = arr_imageUri.lastIndex

        //最初の画像の場合は、最後の画像にする
        if (cnt == 0){

            cnt = lastCnt

            imageView.setImageURI(arr_imageUri[cnt])

        } else {

            //最初でない場合は、カウントを下げ、前の画像を表示する
            -- cnt

            imageView.setImageURI(arr_imageUri[cnt])
        }

        Log.d("test1","戻る終了"+cnt.toString())  //************Log
    }


    //進むボタン押下時のメソッド
    private fun putNextButton(){

        Log.d("test1","進む開始"+cnt.toString())  //************Log

        //画像が存在しない場合、再生中でない場合は処理なしで返却する
        if (arr_imageUri.isEmpty() || mTimer != null){

            return
        }

        //画像URIを格納する配列の最大カウント取得
        val lastCnt = arr_imageUri.lastIndex

        //最後の画像の場合は、最初に戻す
        if (cnt == lastCnt){

            cnt = 0

            imageView.setImageURI(arr_imageUri[cnt])

        } else{

            //最後でない場合は、カウントを進め、次の画像を表示する
            ++ cnt

            imageView.setImageURI(arr_imageUri[cnt])
        }

        Log.d("test1","進む終了"+cnt.toString())  //************Log
    }


    //再生ボタン押下時のメソッド
    private fun putStartButton(){

        //画像URIを格納する配列の最大カウント取得
        val lastCnt = arr_imageUri.lastIndex

        //画像が存在しない場合または、最後のスライドの場合は処理なしで返却する
        if (arr_imageUri.isEmpty() || cnt == lastCnt){

            return
        }

        //再生中かどうかのチェックを行う
        if (mTimer == null){

            //再生されていない場合の処理
            //タイマーオブジェクト生成
            mTimer = Timer()

            //再生ボタンを停止ボタンに変更する
            start_button.text ="停止"

            //再生処理を開始する
            mTimer!!.schedule(object : TimerTask(){

                override fun run() {
                    //最後の画像の場合は、再生を終了
                    if (cnt == lastCnt){

                        mTimer!!.cancel()

                        mTimer = null

                        mHandler.post{

                            start_button.text = "再生"
                        }

                    } else{

                        //最後でない場合は、カウントを進め、次の画像を表示する
                        ++ cnt

                        mHandler.post{

                            imageView.setImageURI(arr_imageUri[cnt])
                        }
                    }
                }
            }, 2000,2000) // 最初に始動させるまで 2秒、ループの間隔を 2秒 に設定

        } else {
            //再生中の場合は、再生を終了する
            mTimer!!.cancel()

            mTimer = null

            start_button.text = "再生"
        }


    }
}
