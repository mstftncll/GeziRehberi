package com.mustafatuncel.turrehberi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_row.view.*

class ListeRecyclerAdapter(val illistesi:ArrayList<String>,val idlistesi:ArrayList<Int>) :RecyclerView.Adapter<ListeRecyclerAdapter.YerHolder>(){
    class YerHolder(itemView:View):RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YerHolder {
        val inflater=LayoutInflater.from(parent.context)
        val view=inflater.inflate(R.layout.recycler_row,parent,false)
        return  YerHolder(view)
    }

    override fun onBindViewHolder(holder: YerHolder, position: Int) {
       holder.itemView.recycler_row_tex.text=illistesi[position]

        holder.itemView.setOnClickListener{
        //recyler icindeki listelere tıklandıgı zaman olaylar
            val action=ListeFragmentDirections.actionListeFragmentToGeziFragment("recyclerdangeldim",idlistesi[position])
            Navigation.findNavController(it).navigate(action)
        }
    }


    override fun getItemCount(): Int {// kac tane recyler row
       return illistesi.size
    }
}