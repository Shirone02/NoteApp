package com.example.noteapp.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.R
import com.example.noteapp.activities.EditNoteActivity
import com.example.noteapp.activities.MainActivity
import com.example.noteapp.adapter.ListNoteAdapter
import com.example.noteapp.databinding.FragmentUncategorizedBinding
import com.example.noteapp.listeners.OnItemClickListener
import com.example.noteapp.models.Note
import com.example.noteapp.viewmodel.NoteViewModel

class UncategorizedFragment : Fragment(R.layout.fragment_uncategorized) {

    private val binding: FragmentUncategorizedBinding by lazy {
        FragmentUncategorizedBinding.inflate(layoutInflater)
    }

    private lateinit var noteViewModel: NoteViewModel
    private lateinit var noteAdapter: ListNoteAdapter
    private lateinit var uncategorizedView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteViewModel = (activity as MainActivity).noteViewModel
        uncategorizedView = view
        setUpNoteRecyclerView()
    }

    private fun setUpNoteRecyclerView() {
        noteAdapter = ListNoteAdapter(object : OnItemClickListener {
            override fun onNoteClick(note: Note, isChoose: Boolean) {
                if (!isChoose) {
                    val intent = Intent(activity, EditNoteActivity::class.java)
                    intent.putExtra("id", note.id)
                    intent.putExtra("title", note.title)
                    intent.putExtra("content", note.content)
                    startActivity(intent)
                }
            }
            override fun onNoteLongClick(note: Note) {

            }
        })

        binding.uncategorizedNote.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.uncategorizedNote.adapter = noteAdapter

        activity?.let {
            noteViewModel.getNotesByCategory(null).observe(viewLifecycleOwner){note ->
                noteAdapter.differ.submitList(note)
                Log.d("TAG", "setUpNoteRecyclerView: $note")
                updateUI(note)
            }
        }

    }

    private fun updateUI(note: List<Note>){
        if(note.isNotEmpty()){
            binding.uncategorizedNote.visibility = View.VISIBLE
        } else {
            binding.uncategorizedNote.visibility = View.GONE
        }
    }

}