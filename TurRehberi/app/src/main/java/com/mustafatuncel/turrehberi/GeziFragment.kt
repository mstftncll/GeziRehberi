package com.mustafatuncel.turrehberi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_gezi.*
import java.io.ByteArrayOutputStream


class GeziFragment : Fragment() {

    var secilengorsel :Uri? =null
    var secilenBitmap:Bitmap?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gezi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button.setOnClickListener {
            kaydet(it)
        }
        imageView.setOnClickListener(){
            gorselsec(it)
        }
        arguments?.let {
            var gelenbilgi=GeziFragmentArgs.fromBundle(it).bilgi
            if (gelenbilgi.equals("menudengeldim")){
                //yeni bir yemek eklemeye geldim
                iltext.setText("")
                yertext.setText("")
                button.visibility=View.VISIBLE
            }else{
                //rcyclerden olan yemegi gormeye geldim
                button.visibility=View.INVISIBLE
                val secilenid=GeziFragmentArgs.fromBundle(it).id
                context?.let {
                    try {
                        val db=it.openOrCreateDatabase("GezilecekYer",Context.MODE_PRIVATE,null)
                        val cursor=db.rawQuery("SELECT * FROM Gezilecekyer WHERE id=?", arrayOf(secilenid.toString()))

                        val ilismindex=cursor.getColumnIndex("il")
                        val yerismiindex=cursor.getColumnIndex("yer")
                        val gorsel=cursor.getColumnIndex("gorsel")

                        while (cursor.moveToNext()){
                            iltext.setText(cursor.getString(ilismindex))
                            yertext.setText(cursor.getString(yerismiindex))

                            val bytedizisi=cursor.getBlob(gorsel)
                            val bitmap=BitmapFactory.decodeByteArray(bytedizisi,0,bytedizisi.size)
                            imageView.setImageBitmap(bitmap)
                        }
                        cursor.close()

                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }

        }
    }
    //gelen veriler 1mb kucuk olmalı veri boyutu dusme
    fun kaydet(view: View){
//sqlite kaydetme
        val ilismi=iltext.text.toString()
        val geziyer=yertext.text.toString()
            if(secilenBitmap!=null){
                val kucukbitmap=resimboyutudusurme(secilenBitmap!!,300)
                //bitmap>veri veriye cevirme
                val outputStream=ByteArrayOutputStream()
                kucukbitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
                val bytedizi=outputStream.toByteArray()

                //sqlite kaydetme
               try {
                    context?.let{
                        val database=it.openOrCreateDatabase("GezilecekYer",Context.MODE_PRIVATE,null)
                        database.execSQL("CREATE TABLE IF NOT EXISTS GezilecekYer(id INTEGER PRIMARY KEY,il VARCHAR,yer VARCHAR,gorsel BLOB)")
                        val sqlString="INSERT INTO GezilecekYer(il,yer,gorsel) VALUES(?,?,?)"
                        val statement=database.compileStatement(sqlString)
                        statement.bindString(1,ilismi)
                        statement.bindString(2,geziyer)
                        statement.bindBlob(3,bytedizi)
                        statement.execute()
                   }

               }catch (e : Exception){
                   e.printStackTrace()
               }
                //kayı isşemi bittikten sonra liste fragmentine geri doncez
                val action=GeziFragmentDirections.actionGeziFragmentToListeFragment()
                Navigation.findNavController(view).navigate(action)

            }
    }
    //resim kucultme funksiyonu
    fun resimboyutudusurme(kullanıcınınSectigiBitmap:Bitmap, maximumBoyut:Int):Bitmap{
        var width=kullanıcınınSectigiBitmap.width
        var height=kullanıcınınSectigiBitmap.height
        val bitmaporan :Double=width.toDouble() / height.toDouble()
        if(bitmaporan>1){
            //gorsel yatay
            width=maximumBoyut
            val kisaltilmisHeight=width/bitmaporan
            height=kisaltilmisHeight.toInt()
        }else{
            //gorsel dıkey
            height=maximumBoyut
            val kisaltilmisWidth=height*bitmaporan
            width=kisaltilmisWidth.toInt()
        }
        return Bitmap.createScaledBitmap(kullanıcınınSectigiBitmap,width,height,true)
    }


    fun gorselsec(view: View){
        //İZİn kontrolü Galeriye erisim izni var mı
        activity?.let {
            if(ContextCompat.checkSelfPermission(it.applicationContext,android.Manifest.permission.READ_EXTERNAL_STORAGE) !=PackageManager.PERMISSION_GRANTED){
                //yoksa yapılacaklar
                    //requset permissions izin sorgu
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
            }
            else{
                //varsa yapılacaklar galeriye git intent lazım
                    //media klasorundan resim pıckle ve adresini al galeriye git kodu
                val galerint=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galerint,2)
            }
        }
    }
//istenilen izinlerin sonucları
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
       if(requestCode==1)
       {
        if (grantResults.size>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
        //izni alındıktan sonra yapılan izin alında ise geri donusu ne
        }
           val galerint=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
           startActivityForResult(galerint,2)
       }
       super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode==2 && resultCode==Activity.RESULT_OK && data !=null)//kullanıcı resulcode bir sey sectimi data d ageri bir vri dondumu
        {
            //secilen gorselin yeri  yolu ögrendikten sonra bitmape ceviriyoruz
            secilengorsel=data.data //gelen veri secilen gorsel oluyor

            try {
                context?.let {
                    //fragment oldugumuz icin context boyle alıyoruz
                    if (secilengorsel !=null) {
                        if (Build.VERSION.SDK_INT>=28){
                            //kayanak olustur resmi bitmape donustur ve al
                            val source= ImageDecoder.createSource(it.contentResolver, secilengorsel!!)
                            secilenBitmap=ImageDecoder.decodeBitmap(source)
                            imageView.setImageBitmap(secilenBitmap)
                        }else{
                            secilenBitmap=MediaStore.Images.Media.getBitmap(it.contentResolver,secilengorsel)
                            imageView.setImageBitmap(secilenBitmap)
                        }
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }



}