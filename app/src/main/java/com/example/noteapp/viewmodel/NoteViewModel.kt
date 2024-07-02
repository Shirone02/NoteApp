package com.example.noteapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.models.Note
import com.example.noteapp.repository.NoteRepository
import kotlinx.coroutines.launch

class NoteViewModel (app: Application, private val noteRepository: NoteRepository): AndroidViewModel(app){
    fun addNote(note: Note) = viewModelScope.launch {
        noteRepository.insertNote(note)
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        noteRepository.deleteNote(note)
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        noteRepository.updateNote(note)
    }

    fun getAllNote() = noteRepository.getAllNote()

    fun searchNote(query: String) = noteRepository.searchNote(query)

    fun deleteNotes(ids: List<Int>) = viewModelScope.launch {
        noteRepository.deleteByIds(ids)
    }
}