package com.mustafatuncel.turrehberi

import android.content.AbstractThreadedSyncAdapter
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_liste.*


class ListeFragment : Fragment() {

    var ilismiliste=ArrayList<String>()
    var yeridListesi=ArrayList<Int>()
    private lateinit var listeAdapter :ListeRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_liste, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listeAdapter=ListeRecyclerAdapter(ilismiliste,yeridListesi)
        recyclerView.layoutManager=LinearLayoutManager(context)
        recyclerView.adapter=listeAdapter

        sqlerialma()
    }
    fun sqlerialma()
    {
        activity?.let {
            try {
                val database=it.openOrCreateDatabase("GezilecekYer",Context.MODE_PRIVATE,null)
                val cursor=database.rawQuery("SELECT * FROM GezilecekYer",null)
                val ilismiIndex=cursor.getColumnIndex("il")
                val yeridindex=cursor.getColumnIndex("id")
                ilismiliste.clear()
                yeridListesi.clear()

                while (cursor.moveToNext()){
                    ilismiliste.add(cursor.getString(ilismiIndex))
                    yeridListesi.add(cursor.getInt(yeridindex))
                }
                //yeni veri varsa hemen getir
                listeAdapter.notifyDataSetChanged()

            }catch (e :Exception){

            }
        }

    }

}