package com.abw.random_app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class LoadList : AppCompatActivity() {

    private lateinit var recView : RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_list)
        supportActionBar?.setBackgroundDrawable(resources.getDrawable(R.drawable.toolbar_background, null))
        recView = findViewById(R.id.recView)
        recView.adapter = LoadRecViewAdapter(MainActivity.loadListNames, this)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.home -> {
                setResult(1)
                finish()
                true
            }
            else -> {
                setResult(1)
                finish()
                true
            }
        }
    }
    class LoadRecViewAdapter(private val values: MutableList<String>, private val context: Context) :  RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        init {
            this.setHasStableIds(true)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return VHItem(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_list_content, parent, false)
            )
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as VHItem).titleView.text = values[position]
            holder.minus.setOnClickListener {
                AlertDialog.Builder(context).apply {
                    setTitle("Remove")
                    setMessage("Are you sure?")
                    setPositiveButton("OK") { _, _ ->
                        MainActivity.loadListNames.remove(values[position])
                        MainActivity.prefs.edit().putStringSet(MainActivity.SAVED_LIST_KEY, MainActivity.loadListNames.toMutableSet()).apply()
                        this@LoadRecViewAdapter.notifyDataSetChanged()
                    }
                    setNegativeButton("Cancel") { _, _ -> }
                }.show()
            }
            holder.itemView.tag = values[position]
            holder.itemView.setOnClickListener {
                (context as AppCompatActivity).setResult(0, Intent().putExtra(MainActivity.LIST_TO_LOAD_KEY, it.tag.toString()))
                context.finish()
            }
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }
        override fun getItemCount(): Int {
            return values.size
        }
        class VHItem(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val titleView: TextView = itemView.findViewById(R.id.titleView)
            val minus : ImageView = itemView.findViewById(R.id.minus)
        }
    }
}

