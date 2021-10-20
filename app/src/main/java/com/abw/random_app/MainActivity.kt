package com.abw.random_app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.DocumentsContract
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileInputStream
import java.lang.Exception
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    companion object {
        var itemList :MutableList<String> = mutableListOf()
        lateinit var pullToRefresh : WeakReference<SwipeRefreshLayout>
        var stringList = ""
        const val separator = "=.="
        var loadListNames = mutableListOf<String>()
        const val SAVED_LIST_KEY = "saved_lists"
        const val LIST_TO_LOAD_KEY = "list_to_load"
        const val DEFAULT_KEY = "default"
        lateinit var prefs : SharedPreferences
        var hasStarted = false

        const val OPEN_FILE = 2
    }

    private lateinit var recView :RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        loadListNames = prefs.getStringSet(SAVED_LIST_KEY, mutableSetOf())!!.toMutableList()
        linkViewProperties()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OPEN_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { inputUri ->
                try {
                    var fileStr = ""
                    this.contentResolver.openFileDescriptor(inputUri, "r")?.use {
                        FileInputStream(it.fileDescriptor).use { inputStream ->
                            val buffer = StringBuffer()
                            val reader = inputStream.bufferedReader()
                            var line = reader.readLine()
                            while (line != null) {
                                buffer.append(line + '\n')
                                line = reader.readLine()
                            }
                            fileStr = buffer.toString()
                        }
                    }

                    val tempList = fileStr.split('\n')
                    itemList = mutableListOf()
                    for (el in tempList) {
                        if (el.isNotEmpty())
                            itemList.add(el)
                    }
                    //saveList("test", itemList)
                    recView.adapter = RecViewAdapter(itemList, this)

                } catch (e: Exception) {
                }
            }
        }

        when(requestCode) {
            0 -> {
                if (resultCode != 1) {
                    val listToLoad = data?.getStringExtra(LIST_TO_LOAD_KEY)
                    val str = prefs.getString(listToLoad, "default")

                    val tempList = str!!.split(separator)
                    itemList = mutableListOf()
                    for (el in tempList) {
                        itemList.add(el)
                    }

                    setupList(listToLoad)
                }
            }
            1 -> {
                if (resultCode != 1) {
                    val listToLoad = data?.getStringExtra(LIST_TO_LOAD_KEY)
                    val str = prefs.getString(listToLoad, "default")

                    val tempList = str!!.split(separator)
                    itemList = mutableListOf()
                    for (el in tempList) {
                        itemList.add(el)
                    }

                    setupList(listToLoad)
                }
            }
        }
    }

    fun setupList(listToLoad: String?) {
        recView.adapter = RecViewAdapter(itemList, this)
        prefs.edit().putString(DEFAULT_KEY, listToLoad).apply()
    }

    override fun onStop() {
        super.onStop()
        if (loadListNames.isEmpty())
            prefs.edit().putString(DEFAULT_KEY, "").apply()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.remove -> {
                AlertDialog.Builder(this).apply {
                    setTitle("Remove all items")
                    setMessage("Are you sure?")
                    setPositiveButton("OK") { _, _ ->
                        itemList.clear()
                        recView.adapter = RecViewAdapter(itemList, this@MainActivity)
                        Toast.makeText(this@MainActivity, "Deleted All", Toast.LENGTH_SHORT).show()
                    }
                    setNegativeButton("Cancel") { _, _ -> }
                }.show()
                true
            }
            R.id.shuffle -> {
                itemList.shuffle()
                recView.adapter?.notifyDataSetChanged()
                Toast.makeText(this@MainActivity, "Shuffled", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.toss -> {
                val intent = Intent(this, CoinToss::class.java)
                startActivityForResult(intent, 1)
                true
            }
            R.id.range -> {
                val intent = Intent(this, RangeActivity::class.java)
                startActivityForResult(intent, 1)
                true
            }
            R.id.load -> {
                val intent = Intent(this, LoadList::class.java)
                startActivityForResult(intent, 0)
                true
            }
            R.id.import_list -> {
                val uri = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.toUri()
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
                }
                startActivityForResult(intent, OPEN_FILE)
                true
            }
            R.id.save -> {
                if (itemList.isNotEmpty()) {
                    val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    val alert = AlertDialog.Builder(this)
                    val container = LinearLayout(this)
                    val editText = EditText(this)
                    val params = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    params.leftMargin = resources.getDimensionPixelSize(R.dimen.edit_text_margin)
                    params.rightMargin = resources.getDimensionPixelSize(R.dimen.edit_text_margin)
                    params.bottomMargin = resources.getDimensionPixelSize(R.dimen.edit_text_margin)
                    editText.layoutParams = params
                    editText.setSingleLine()
                    container.addView(editText)
                    val dialog = alert.apply {
                        setTitle("Save List As...")
                        setView(container)
                        setPositiveButton("OK") { _, _ ->
                            saveList(editText.text.toString(), itemList)
                        }
                        setNegativeButton("Cancel") { _, _ ->
                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                        }
                    }.create()
                    dialog.show()
                    editText.setOnEditorActionListener { view, actionId, _ ->
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            stringList = ""
                            for ((count, el) in itemList.withIndex()) {
                                stringList += el
                                if (count + 1 != itemList.size)
                                    stringList += separator
                            }
                            val savedLists = prefs.getStringSet(SAVED_LIST_KEY, mutableSetOf())
                            savedLists?.add(editText.text.toString())
                            loadListNames = savedLists!!.toMutableList()
                            prefs.edit().putStringSet(SAVED_LIST_KEY, savedLists).apply()
                            prefs.edit().putString(editText.text.toString(), stringList).apply()
                            prefs.edit().putString(DEFAULT_KEY, stringList).apply()
                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                            dialog.cancel()
                            Toast.makeText(this@MainActivity, "Saved List", Toast.LENGTH_SHORT).show()
                        }
                        return@setOnEditorActionListener false
                    }
                    editText.requestFocus()
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
                    true
                }
                else {
                    Toast.makeText(this, "Cannot save empty lists", Toast.LENGTH_SHORT).show()
                    true
                }
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun saveList(str: String, list: MutableList<String>) {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (str.isNotEmpty()) {
            stringList = ""
            for ((count, el) in list.withIndex()) {
                stringList += el
                if (count + 1 != list.size)
                    stringList += separator
            }
            val savedLists = prefs.getStringSet(SAVED_LIST_KEY, mutableSetOf())
            savedLists?.add(str)
            loadListNames = savedLists!!.toMutableList()
            prefs.edit().putStringSet(SAVED_LIST_KEY, savedLists).apply()
            prefs.edit().putString(str, stringList).apply()
            prefs.edit().putString(DEFAULT_KEY, stringList).apply()
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
            Toast.makeText(this@MainActivity, "Saved List", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(this@MainActivity, "Name must be at least 1 character", Toast.LENGTH_SHORT).show()
        }
    }

    fun linkViewProperties() {
        setSupportActionBar(toolbar)
        supportActionBar?.setBackgroundDrawable(resources.getDrawable(R.drawable.toolbar_background, null))

        recView = findViewById(R.id.recView)
        pullToRefresh = WeakReference(findViewById(R.id.swipeRefresh))

        if (!hasStarted) {
            hasStarted = true
            val defaultList = prefs.getString(DEFAULT_KEY, "default")
            val str = prefs.getString(defaultList, "")
            val arr = str?.split(separator) as MutableList<String>
            if (str != "")
                itemList = arr.toMutableList()
            recView.adapter = RecViewAdapter(itemList, this)
        }

        pullToRefresh.get()?.apply {
            setOnRefreshListener {
                itemList.shuffle()
                recView.adapter?.notifyDataSetChanged()
                this.isRefreshing = false
                Toast.makeText(this@MainActivity, "Shuffled", Toast.LENGTH_SHORT).show()
            }
            setColorSchemeColors(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary))
        }

        fab.setOnClickListener { _ ->
            val alert = AlertDialog.Builder(this)
            val container = LinearLayout(this)
            val editText = EditText(this)
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.leftMargin = resources.getDimensionPixelSize(R.dimen.edit_text_margin)
            params.rightMargin = resources.getDimensionPixelSize(R.dimen.edit_text_margin)
            params.bottomMargin = resources.getDimensionPixelSize(R.dimen.edit_text_margin)
            editText.layoutParams = params
            editText.setSingleLine()
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            container.addView(editText)
            val dialog = alert.apply {
                setTitle("Add a new item to the list")
                setView(container)
                setPositiveButton("OK") { _, _ ->
                    itemList.add(editText.text.toString())
                    recView.adapter?.notifyDataSetChanged()
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                }
                setNegativeButton("Cancel") { _, _ ->
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                }
            }.create()
            dialog.show()
            editText.setOnEditorActionListener { view, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    itemList.add(view.text.toString())
                    dialog.cancel()
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                }
                return@setOnEditorActionListener false
            }
            editText.requestFocus()
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
    }

    class RecViewAdapter(private val values: List<String>, private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
            if (position == 0) {
                holder.titleView.setTextColor(context.resources.getColor(R.color.colorWhite, null))
                holder.cardView.background = context.resources.getDrawable(R.drawable.winner, null)
                holder.minus.visibility = View.INVISIBLE
                holder.goldMinus.visibility = View.VISIBLE
                holder.goldMinus.setOnClickListener {
                    AlertDialog.Builder(context).apply {
                        setTitle("Remove")
                        setMessage("Are you sure?")
                        setPositiveButton("OK") { _, _ ->
                            itemList.removeAt(position)
                            this@RecViewAdapter.notifyDataSetChanged()
                        }
                        setNegativeButton("Cancel") { _, _ -> }
                    }.show()
                }
            }
            holder.minus.setOnClickListener {
                AlertDialog.Builder(context).apply {
                    setTitle("Remove")
                    setMessage("Are you sure?")
                    setPositiveButton("OK") { _, _ ->
                        itemList.removeAt(position)
                        this@RecViewAdapter.notifyDataSetChanged()
                    }
                    setNegativeButton("Cancel") { _, _ -> }
                }.show()
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
            val cardView: CardView = itemView.findViewById(R.id.cardView)
            val minus : ImageView = itemView.findViewById(R.id.minus)
            val goldMinus :ImageView = itemView.findViewById(R.id.gold_minus)
        }
    }
}