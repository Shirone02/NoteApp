package com.example.noteapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.util.query
import com.example.noteapp.R
import com.example.noteapp.adapter.ListNoteAdapter
import com.example.noteapp.database.NoteDatabase
import com.example.noteapp.databinding.ActivityMainBinding
import com.example.noteapp.fragments.BackupFragment
import com.example.noteapp.fragments.EditCategoriesFragment
import com.example.noteapp.fragments.NotesFragment
import com.example.noteapp.fragments.TrashFragment
import com.example.noteapp.fragments.UncategorizedFragment
import com.example.noteapp.listeners.OnItemClickListener
import com.example.noteapp.models.Note
import com.example.noteapp.repository.NoteRepository
import com.example.noteapp.viewmodel.NoteViewModel
import com.example.noteapp.viewmodel.NoteViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import java.util.ArrayList
import java.util.Calendar


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var noteViewModel: NoteViewModel
    private lateinit var noteAdapter: ListNoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setUpViewModel()

        setUpNoteRecyclerView()

        binding.addNoteFab.setOnClickListener {
            addNote()
        }

        setSupportActionBar(binding.topAppBar)

        binding.navView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.topAppBar, R.string.open_nav, R.string.close_nav)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NotesFragment()).commit()
            binding.navView.setCheckedItem(R.id.nav_note)
        }

        binding.topAppBar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.white))

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.search -> {
                    val searchView = menuItem.actionView as SearchView
                    val currentList = noteAdapter.differ.currentList

                    searchView.setOnQueryTextListener(object : OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            if (newText.isNullOrEmpty()) {
                                noteAdapter.differ.submitList(currentList)
                            } else {
                                filterList(newText)
                            }
                            Log.d("TAG", "onQueryTextChange: $newText")
                            return true
                        }

                    })
                    true
                }

                R.id.sort -> {
                    showOptionDialog()
                    true
                }

                R.id.delete -> {
                    showDeleteDialog()
                    true
                }

                R.id.selectAll -> {
                    noteAdapter.selectAllItem()
                    true
                }

                else -> {
                    false
                }
            }
        }

    }

    private fun showDeleteDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        builder.setTitle("Delete")
            .setMessage("Do you want to delete?")
            .setPositiveButton("Delete") { dialog, which ->
                deleteSelectedItem()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun deleteSelectedItem() {
        val selectedNotes = noteAdapter.getSelectedItems()
        val selectedIds = selectedNotes.map { it.id }
        noteViewModel.deleteNotes(selectedIds)
        noteAdapter.removeSelectedItems()
    }

    private fun showDeleteIcon(){
        binding.topAppBar.menu.clear()
        binding.topAppBar.inflateMenu(R.menu.menu_selection)
        binding.topAppBar.setTitle("${noteAdapter.getSelectedItemsCount()}")
        binding.topAppBar.setNavigationIcon(R.drawable.ic_back)
        binding.topAppBar.setNavigationOnClickListener {
            noteAdapter.clearSelection()
            binding.topAppBar.menu.clear()
            binding.topAppBar.inflateMenu(R.menu.top_app_bar)
            binding.topAppBar.setNavigationIcon(R.drawable.ic_option)
            binding.topAppBar.setTitle("Notepad Free")
        }
    }

    private fun showOptionDialog() {
        val sortOption = arrayOf(
            "edit date: from newest",
            "edit date: from oldest",
            "title: A to Z",
            "title: Z to A",
            "creation date: from newest",
            "creation date: from oldest"
        )

        var selectedOption = 0
        val noteList = noteAdapter.differ.currentList
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Sort by")
            .setPositiveButton("Sort") { dialog, which ->
                when (selectedOption) {
                    0 -> sortByEditDateNewest(noteList)
                    1 -> sortByEditDateOldest(noteList)
                    2 -> sortByTitleAToZ(noteList)
                    3 -> sortByTitleZToA(noteList)
                    4 -> sortByCreationDateNewest(noteList)
                    5 -> sortByCreationDateOldest(noteList)
                }
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .setSingleChoiceItems(sortOption, selectedOption) { dialog, which ->
                selectedOption = which
            }

        builder.create().show()
    }

    private fun sortByCreationDateOldest(noteList: List<Note>) {
        return noteAdapter.differ.submitList(noteList.sortedBy { it.id })
    }

    private fun sortByCreationDateNewest(noteList: List<Note>) {
        return noteAdapter.differ.submitList(noteList.sortedByDescending { it.id })
    }

    private fun sortByTitleZToA(noteList: List<Note>) {
        return noteAdapter.differ.submitList(noteList.sortedByDescending { it.title })
    }

    private fun sortByTitleAToZ(noteList: List<Note>) {
        return noteAdapter.differ.submitList(noteList.sortedBy { it.title })
    }

    private fun sortByEditDateOldest(noteList: List<Note>) {
        return noteAdapter.differ.submitList(noteList.sortedBy { it.time })
    }

    private fun sortByEditDateNewest(noteList: List<Note>) {
        return noteAdapter.differ.submitList(noteList.sortedByDescending { it.time })
    }

    private fun filterList(newText: String?) {
        noteViewModel.searchNote("%" + newText!! + "%").observe(this) { notes ->
            noteAdapter.differ.submitList(notes)
        }
    }

    private fun addNote() {
        val note = Note(0, "Untitled", "", getCurrentTime())
        noteViewModel.addNote(note)

        val intent = Intent(this@MainActivity, EditNoteActivity::class.java)
        intent.putExtra("id", note.id)
        intent.putExtra("title", note.title)
        intent.putExtra("content", note.content)
        startActivity(intent)

        Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
    }

    private fun setUpNoteRecyclerView() {
        noteAdapter = ListNoteAdapter(object : OnItemClickListener {
            override fun onNoteClick(note: Note, isChoose: Boolean) {
                if (!isChoose) {
                    val intent = Intent(this@MainActivity, EditNoteActivity::class.java)
                    intent.putExtra("id", note.id)
                    intent.putExtra("title", note.title)
                    intent.putExtra("content", note.content)
                    startActivity(intent)
                }
            }
            override fun onNoteLongClick(note: Note) {
                showDeleteIcon()
            }
        })

        binding.listNoteRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.listNoteRecyclerView.adapter = noteAdapter

        this.let {
            noteViewModel.getAllNote().observe(this) { note ->
                noteAdapter.differ.submitList(note)
                updateUI(note)
            }
        }
    }

    private fun updateUI(note: List<Note>?) {
        if (note != null) {
            if (note.isNotEmpty()) {
                binding.listNoteRecyclerView.visibility = View.VISIBLE
            } else {
                binding.listNoteRecyclerView.visibility = View.GONE
            }
        }

    }

    private fun setUpViewModel() {
        val noteRepository = NoteRepository(NoteDatabase(this))

        val viewModelProviderFactory = NoteViewModelFactory(application, noteRepository)

        noteViewModel = ViewModelProvider(this, viewModelProviderFactory)[NoteViewModel::class.java]
    }

    private fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Tháng trong Calendar bắt đầu từ 0
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        val formattedDate = "$day/$month/$year $hour:$minute:$second"

        return formattedDate
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_note -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, NotesFragment()).commit()
            R.id.nav_edit_categories -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, EditCategoriesFragment()).commit()
            R.id.nav_backup -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, BackupFragment()).commit()
            R.id.nav_trash -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, TrashFragment()).commit()
            R.id.nav_uncategorized -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, UncategorizedFragment()).commit()

        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if(binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}