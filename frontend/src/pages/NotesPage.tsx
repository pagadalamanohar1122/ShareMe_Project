import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { ArrowLeft, Save, Tag, Search, FileText, Paperclip, Upload, X, Edit3, Clock, Trash2 } from 'lucide-react';
import { taskNoteService } from '../services/taskNoteService';
import type { TaskNoteRequest, TaskNote } from '../types/taskNote';

const NotesPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const taskIdParam = searchParams.get('taskId');
  const taskId = taskIdParam ? parseInt(taskIdParam) : null;

  // State for current note being edited
  const [currentNote, setCurrentNote] = useState<{
    noteName: string;
    noteContent: string;
    reminderTags: string[];
    attachments: File[];
    hasExistingNote: boolean;
    existingNoteId?: number;
  }>({
    noteName: '',
    noteContent: '',
    reminderTags: [],
    attachments: [],
    hasExistingNote: false,
    existingNoteId: undefined
  });

  // State for notes list
  const [allNotes, setAllNotes] = useState<TaskNote[]>([]);
  const [filteredNotes, setFilteredNotes] = useState<TaskNote[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [tagSearchQuery, setTagSearchQuery] = useState('');
  const [appliedTagFilter, setAppliedTagFilter] = useState('');

  // UI state
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [newTag, setNewTag] = useState('');
  const [viewingNote, setViewingNote] = useState<TaskNote | null>(null);
  const [isViewModalOpen, setIsViewModalOpen] = useState(false);

  // Load data on component mount
  useEffect(() => {
    loadAllNotes();
    createNewNote();
  }, []);

  // Filter notes based on search, tag filter, and current taskId from URL
  useEffect(() => {
    let filtered = allNotes;
    
    // Filter by current task from URL parameter
    if (taskId !== null) {
      filtered = filtered.filter(note => note.taskId === taskId);
    } else {
      // If no task is selected, show only standalone notes (notes without taskId)
      filtered = filtered.filter(note => note.taskId === null || note.taskId === undefined);
    }
    
    if (searchQuery) {
      filtered = filtered.filter(note => 
        (note.noteName?.toLowerCase().includes(searchQuery.toLowerCase())) ||
        note.noteContent.toLowerCase().includes(searchQuery.toLowerCase())
      );
    }
    
    if (appliedTagFilter) {
      filtered = filtered.filter(note => 
        note.reminderTags?.some(tag => 
          tag.toLowerCase().includes(appliedTagFilter.toLowerCase())
        )
      );
    }
    
    setFilteredNotes(filtered);
  }, [allNotes, searchQuery, appliedTagFilter, taskId]);

  const handleTagSearch = () => {
    setAppliedTagFilter(tagSearchQuery.trim());
  };

  const clearTagFilter = () => {
    setTagSearchQuery('');
    setAppliedTagFilter('');
  };

  const handleTagSearchKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleTagSearch();
    }
  };

  const loadAllNotes = async () => {
    try {
      setLoading(true);
      const notes = await taskNoteService.getAllUserNotes();
      setAllNotes(notes || []);
    } catch (err) {
      setAllNotes([]);
    } finally {
      setLoading(false);
    }
  };

  const selectNoteForEditing = (note: TaskNote) => {
    setCurrentNote({
      existingNoteId: note.id || undefined,
      noteName: note.noteName || '',
      noteContent: note.noteContent || '',
      reminderTags: note.reminderTags || [],
      attachments: [],
      hasExistingNote: true
    });
  };

  const openNoteViewModal = (note: TaskNote) => {
    setViewingNote(note);
    setIsViewModalOpen(true);
  };

  const closeNoteViewModal = () => {
    setViewingNote(null);
    setIsViewModalOpen(false);
  };  const handleSave = async () => {
    if (!currentNote.noteContent.trim() && currentNote.reminderTags.length === 0) {
      setError('Please enter some content or add tags before saving.');
      return;
    }

    try {
      setSaving(true);
      setError('');
      
      const request: TaskNoteRequest = {
        taskId: taskId, // Use the taskId from URL parameter
        noteName: currentNote.noteName || undefined,
        noteContent: currentNote.noteContent,
        reminderTags: currentNote.reminderTags
      };

      await taskNoteService.saveTaskNote(request);
      await loadAllNotes();
      setSuccess('Note saved successfully!');
      
      // Reset form for new note
      createNewNote();
      
      // Clear success message after 3 seconds
      setTimeout(() => setSuccess(''), 3000);
    } catch (err: any) {
      console.error('Failed to save note:', err);
      setError('Failed to save note. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  const createNewNote = () => {
    setCurrentNote({
      noteName: '',
      noteContent: '',
      reminderTags: [],
      attachments: [],
      hasExistingNote: false,
      existingNoteId: undefined
    });
    setError('');
    setSuccess('');
  };

  const addTag = (tag: string) => {
    if (tag.trim() && !currentNote.reminderTags.includes(tag.trim())) {
      setCurrentNote(prev => ({
        ...prev,
        reminderTags: [...prev.reminderTags, tag.trim()]
      }));
      setNewTag('');
    }
  };

  const removeTag = (tagToRemove: string) => {
    setCurrentNote(prev => ({
      ...prev,
      reminderTags: prev.reminderTags.filter(tag => tag !== tagToRemove)
    }));
  };

  // Helper function to format dates
  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && newTag.trim()) {
      addTag(newTag);
    }
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    setCurrentNote(prev => ({
      ...prev,
      attachments: [...prev.attachments, ...files]
    }));
  };

  const removeAttachment = (index: number) => {
    setCurrentNote(prev => ({
      ...prev,
      attachments: prev.attachments.filter((_, i) => i !== index)
    }));
  };

  const handleDeleteNote = async (noteId: number) => {
    if (!window.confirm('Are you sure you want to delete this note?')) {
      return;
    }

    try {
      // For localStorage implementation, we need to call the delete method
      const notes = await taskNoteService.getAllUserNotes();
      const updatedNotes = notes.filter(note => note.id !== noteId);
      localStorage.setItem('task_notes', JSON.stringify(updatedNotes));
      
      // Reload notes
      await loadAllNotes();
      setSuccess('Note deleted successfully!');
      
      // If we were editing this note, reset the form
      if (currentNote.existingNoteId === noteId) {
        createNewNote();
      }
      
      // Clear success message after 3 seconds
      setTimeout(() => setSuccess(''), 3000);
    } catch (err: any) {
      console.error('Failed to delete note:', err);
      setError('Failed to delete note. Please try again.');
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      {/* Header */}
      <div className="bg-white shadow-lg border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center">
              <button
                onClick={() => navigate('/tasks')}
                className="mr-4 p-2 hover:bg-gray-100 rounded-lg transition-colors"
              >
                <ArrowLeft className="h-5 w-5 text-gray-600" />
              </button>
              <div className="flex items-center space-x-3">
                <div className="bg-blue-600 text-white p-2 rounded-lg">
                  <FileText className="h-6 w-6" />
                </div>
                <div>
                  <h1 className="text-xl font-semibold text-gray-900">Documentation Center</h1>
                  <p className="text-xs text-gray-500">Enterprise Task Management</p>
                </div>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <div className="flex items-center text-sm text-gray-600 bg-gray-50 px-3 py-1 rounded-full">
                <span className="font-medium">Production Ready</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="flex h-[calc(100vh-4rem)] max-w-7xl mx-auto gap-6 p-6">
        {/* Left Side - Note Input */}
        <div className="w-1/2 bg-white border border-gray-200 flex flex-col shadow-lg rounded-xl">
          <div className="px-6 py-4 border-b border-gray-200 bg-white rounded-t-xl">
            <div className="flex items-center justify-between">
              <div className="flex-1">
                <div className="flex items-center space-x-3 mb-2">
                  <div className="bg-blue-600 text-white p-2 rounded-lg">
                    <FileText className="h-5 w-5" />
                  </div>
                  <div>
                    <h2 className="text-lg font-semibold text-gray-900">
                      Create Documentation
                    </h2>
                  </div>
                </div>
                <p className="text-sm text-gray-600 ml-12">
                  Add new documentation and notes
                </p>
              </div>
              <div className="flex items-center text-xs text-gray-600 bg-gray-50 px-3 py-1 rounded-full">
                <span className="font-medium">Active</span>
              </div>
            </div>
          </div>

          <div className="px-6 py-3 bg-white border-b border-gray-100">
            {error && (
              <div className="p-3 bg-red-50 border-l-4 border-red-400 text-red-700 text-sm rounded-r-md">
                <div className="flex">
                  <div className="flex-shrink-0">
                    <X className="h-4 w-4 text-red-400" />
                  </div>
                  <div className="ml-2">
                    {error}
                  </div>
                </div>
              </div>
            )}

            {success && (
              <div className="p-3 bg-green-50 border-l-4 border-green-400 text-green-700 text-sm rounded-r-md">
                <div className="flex">
                  <div className="flex-shrink-0">
                    <FileText className="h-4 w-4 text-green-400" />
                  </div>
                  <div className="ml-2">
                    {success}
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Note Form */}
          <div className="flex-1 overflow-y-auto bg-gradient-to-b from-gray-50 to-white">
            <div className="p-6 space-y-8">
              {/* Document Title Section */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                <div className="flex items-center mb-4">
                  <div className="bg-blue-100 p-2 rounded-lg mr-3">
                    <Edit3 className="h-5 w-5 text-blue-600" />
                  </div>
                  <div>
                    <label className="block text-lg font-semibold text-gray-800">
                      Document Title
                    </label>
                    <p className="text-sm text-gray-500">Give your document a descriptive title</p>
                  </div>
                </div>
                <input
                  type="text"
                  value={currentNote.noteName}
                  onChange={(e) => setCurrentNote(prev => ({ ...prev, noteName: e.target.value }))}
                  placeholder="e.g., API Documentation, Meeting Notes, Requirements..."
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 shadow-sm transition-all duration-200 text-gray-900 placeholder-gray-400"
                />
              </div>

              {/* Content Section */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                <div className="flex items-center mb-4">
                  <div className="bg-blue-100 p-2 rounded-lg mr-3">
                    <FileText className="h-5 w-5 text-blue-600" />
                  </div>
                  <div>
                    <label className="block text-lg font-semibold text-gray-800">
                      Content
                    </label>
                    <p className="text-sm text-gray-500">Write your documentation content</p>
                  </div>
                </div>
                <textarea
                  value={currentNote.noteContent}
                  onChange={(e) => setCurrentNote(prev => ({ ...prev, noteContent: e.target.value }))}
                  placeholder="Start writing your documentation here...

You can include:
• Technical specifications
• Implementation details
• Meeting notes
• Requirements
• Any other relevant information"
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 resize-none shadow-sm transition-all duration-200 text-gray-900 placeholder-gray-400"
                  rows={12}
                />
              </div>

              {/* Attachments Section */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                <div className="flex items-center mb-4">
                  <div className="bg-blue-100 p-2 rounded-lg mr-3">
                    <Paperclip className="h-5 w-5 text-blue-600" />
                  </div>
                  <div>
                    <label className="block text-lg font-semibold text-gray-800">
                      Attachments
                    </label>
                    <p className="text-sm text-gray-500">Upload supporting files and documents</p>
                  </div>
                </div>
                
                <div className="border-2 border-dashed border-gray-300 rounded-xl p-8 text-center hover:border-blue-400 hover:bg-blue-50 transition-all duration-200 group">
                  <input
                    type="file"
                    id="fileUpload"
                    multiple
                    onChange={handleFileUpload}
                    className="hidden"
                  />
                  <label
                    htmlFor="fileUpload"
                    className="cursor-pointer flex flex-col items-center"
                  >
                    <div className="bg-gray-100 group-hover:bg-blue-100 p-3 rounded-full mb-3 transition-colors duration-200">
                      <Upload className="h-8 w-8 text-gray-400 group-hover:text-blue-500 transition-colors duration-200" />
                    </div>
                    <span className="text-sm font-medium text-gray-600 group-hover:text-blue-600 transition-colors duration-200">
                      Click to upload files
                    </span>
                    <span className="text-xs text-gray-400 mt-1">
                      PDF, DOC, JPG, PNG up to 10MB
                    </span>
                  </label>
                </div>
                
                {/* Attached Files */}
                {currentNote.attachments.length > 0 && (
                  <div className="mt-4 space-y-2">
                    <h4 className="text-sm font-medium text-gray-700 mb-2">Uploaded Files</h4>
                    {currentNote.attachments.map((file, index) => (
                      <div key={index} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg border border-gray-200 hover:bg-gray-100 transition-colors">
                        <div className="flex items-center">
                          <div className="bg-blue-100 p-1.5 rounded mr-3">
                            <Paperclip className="h-4 w-4 text-blue-600" />
                          </div>
                          <div>
                            <span className="text-sm font-medium text-gray-700">{file.name}</span>
                            <p className="text-xs text-gray-500">{(file.size / 1024).toFixed(1)} KB</p>
                          </div>
                        </div>
                        <button
                          onClick={() => removeAttachment(index)}
                          className="text-red-400 hover:text-red-600 hover:bg-red-50 p-1 rounded transition-colors"
                        >
                          <X className="h-4 w-4" />
                        </button>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Tags Section */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                <div className="flex items-center mb-4">
                  <div className="bg-blue-100 p-2 rounded-lg mr-3">
                    <Tag className="h-5 w-5 text-blue-600" />
                  </div>
                  <div>
                    <label className="block text-lg font-semibold text-gray-800">
                      Tags
                    </label>
                    <p className="text-sm text-gray-500">Organize with labels and categories</p>
                  </div>
                </div>
                
                {/* Existing Tags */}
                {currentNote.reminderTags.length > 0 && (
                  <div className="mb-4">
                    <h4 className="text-sm font-medium text-gray-700 mb-2">Current Tags</h4>
                    <div className="flex flex-wrap gap-2">
                      {currentNote.reminderTags.map((tag, index) => (
                        <span
                          key={index}
                          className="inline-flex items-center px-3 py-1.5 rounded-md text-xs font-medium bg-blue-100 text-blue-700 border border-blue-200"
                        >
                          <Tag className="h-3 w-3 mr-1.5" />
                          {tag}
                          <button
                            onClick={() => removeTag(tag)}
                            className="ml-2 hover:bg-red-100 rounded-full p-0.5 transition-colors"
                          >
                            <X className="h-3 w-3" />
                          </button>
                        </span>
                      ))}
                    </div>
                  </div>
                )}

                {/* Add New Tag */}
                <div className="space-y-3">
                  <h4 className="text-sm font-medium text-gray-700">Add New Tag</h4>
                  <div className="flex gap-3">
                    <div className="flex-1 relative">
                      <input
                        type="text"
                        value={newTag}
                        onChange={(e) => setNewTag(e.target.value)}
                        onKeyPress={handleKeyPress}
                        placeholder="Enter tag name (e.g., urgent, frontend, api...)"
                        className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm shadow-sm transition-all duration-200 placeholder-gray-400"
                      />
                    </div>
                    <button
                      onClick={() => addTag(newTag)}
                      disabled={!newTag.trim()}
                      className="px-6 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed text-sm font-medium transition-all duration-200 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 flex items-center gap-2"
                    >
                      <Tag className="h-4 w-4" />
                      Add Tag
                    </button>
                  </div>
                </div>
              </div>

              {/* Save Button Section */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                <div className="flex items-center justify-between mb-4">
                  <div className="flex items-center">
                    <div className="bg-blue-100 p-2 rounded-lg mr-3">
                      <Save className="h-5 w-5 text-blue-600" />
                    </div>
                    <div>
                      <h3 className="text-lg font-semibold text-gray-800">
                        {currentNote.hasExistingNote ? 'Update Document' : 'Save Document'}
                      </h3>
                      <p className="text-sm text-gray-500">
                        {currentNote.hasExistingNote ? 'Apply changes to your document' : 'Create your new documentation'}
                      </p>
                    </div>
                  </div>
                  <div className="text-sm text-gray-500">
                    {currentNote.noteContent.length} characters
                  </div>
                </div>
                
                <button
                  onClick={handleSave}
                  disabled={saving || (!currentNote.noteContent.trim() && currentNote.reminderTags.length === 0)}
                  className="w-full inline-flex items-center justify-center px-6 py-3 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                >
                  {saving ? (
                    <>
                      <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-3"></div>
                      {currentNote.hasExistingNote ? 'Updating...' : 'Saving...'}
                    </>
                  ) : (
                    <>
                      <Save className="h-5 w-5 mr-3" />
                      {currentNote.hasExistingNote ? 'Update Documentation' : 'Save Documentation'}
                    </>
                  )}
                </button>
                
                {/* Save Info */}
                <div className="mt-3 text-center">
                  <p className="text-xs text-gray-500">
                    Your document will be automatically associated with this task
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Right Side - Notes Display */}
        <div className="w-1/2 bg-white flex flex-col shadow-lg rounded-xl border border-gray-200">
          {/* Search and Filter */}
          <div className="px-6 py-4 border-b border-gray-200 bg-white rounded-t-xl">
            <div className="mb-4">
              <div className="flex items-center justify-between mb-2">
                <div className="flex-1">
                  <div className="flex items-center space-x-3 mb-2">
                    <div className="bg-blue-600 text-white p-2 rounded-lg">
                      <FileText className="h-5 w-5" />
                    </div>
                    <div>
                      <h2 className="text-lg font-semibold text-gray-900">
                        Documentation Repository
                      </h2>
                    </div>
                  </div>
                  <p className="text-sm text-gray-600 ml-12">
                    Centralized documentation for this task
                  </p>
                </div>
                <div className="flex items-center text-xs text-gray-600 bg-gray-50 px-3 py-1 rounded-full">
                  <span className="font-medium">{filteredNotes.length} documents</span>
                </div>
              </div>
            </div>
            
            {/* Search Bar */}
            <div className="relative mb-6">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
              <input
                type="text"
                placeholder="Search documentation..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-400 focus:border-blue-400 shadow-sm transition-all duration-200 text-gray-900 placeholder-gray-500"
              />
            </div>

            {/* Tag Filter */}
            <div className="mb-6">
              <label className="block text-sm font-semibold text-gray-900 mb-3">Filter by Tag</label>
              <div className="flex gap-3">
                <div className="flex-1 relative">
                  <input
                    type="text"
                    placeholder="Enter tag name..."
                    value={tagSearchQuery}
                    onChange={(e) => setTagSearchQuery(e.target.value)}
                    onKeyPress={handleTagSearchKeyPress}
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm shadow-sm transition-all duration-200 placeholder-gray-500"
                  />
                </div>
                <button
                  onClick={handleTagSearch}
                  className="px-5 py-2.5 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-lg hover:from-blue-600 hover:to-blue-700 disabled:opacity-50 disabled:cursor-not-allowed text-sm font-medium transition-all duration-200 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                >
                  <Search className="h-4 w-4" />
                </button>
                {appliedTagFilter && (
                  <button
                    onClick={clearTagFilter}
                    className="px-4 py-2.5 bg-gradient-to-r from-gray-100 to-gray-200 text-gray-700 rounded-lg hover:from-gray-200 hover:to-gray-300 text-sm font-medium transition-all duration-200 shadow-sm"
                  >
                    <X className="h-4 w-4" />
                  </button>
                )}
              </div>
              {appliedTagFilter && (
                <div className="mt-3 text-sm text-gray-600">
                  Filtering by: <span className="font-semibold text-blue-600">"{appliedTagFilter}"</span>
                </div>
              )}
            </div>

            {/* Notes Count */}
            <div className="text-sm font-medium text-gray-700 border-t border-gray-200 pt-4">
              {loading ? (
                <div className="flex items-center">
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600 mr-2"></div>
                  Loading documentation...
                </div>
              ) : (
                `${filteredNotes.length} ${filteredNotes.length === 1 ? 'document' : 'documents'} found`
              )}
            </div>
          </div>

          {/* Notes List */}
          <div className="flex-1 overflow-y-auto p-6 bg-gradient-to-b from-gray-50 to-white">
            {loading ? (
              <div className="flex items-center justify-center py-8">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              </div>
            ) : filteredNotes.length === 0 ? (
              <div className="text-center py-8">
                <FileText className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                <p className="text-gray-500">
                  {taskId 
                    ? `No notes found for this task`
                    : 'No notes found'
                  }
                </p>
                <p className="text-sm text-gray-400 mt-1">
                  {searchQuery || appliedTagFilter 
                    ? 'Try adjusting your search or filter' 
                    : taskId 
                      ? `Create your first note for this task`
                      : 'Create your first note'
                  }
                </p>
              </div>
            ) : (
              <div className="space-y-4">
                {filteredNotes.map((note) => (
                  <div
                    key={note.id}
                    className={`bg-white border rounded-lg p-4 shadow-sm hover:shadow-md transition-all duration-200 ${
                      currentNote.existingNoteId === note.id ? 'border-blue-500 bg-blue-50 shadow-md' : 'border-gray-200 hover:border-gray-300'
                    }`}
                  >
                    {/* Note Header */}
                    <div className="flex items-start justify-between mb-2">
                      <div className="flex-1">
                        <h3 className="font-medium text-gray-900 truncate">
                          {note.noteName || 'Untitled Note'}
                        </h3>
                        <div className="flex items-center gap-2 mt-1">
                          {note.createdAt && (
                            <span className="text-xs text-gray-500 flex items-center">
                              <Clock className="h-3 w-3 mr-1" />
                              {formatDate(note.createdAt)}
                            </span>
                          )}
                        </div>
                      </div>
                      <div className="flex items-center gap-1 ml-3">
                        <button
                          onClick={() => selectNoteForEditing(note)}
                          className="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                          title="Edit note"
                        >
                          <Edit3 className="h-4 w-4" />
                        </button>
                        <button
                          onClick={() => note.id && handleDeleteNote(note.id)}
                          className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                          title="Delete note"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </div>

                    {/* Note Content Preview */}
                    <div className="mb-3">
                      <p className="text-sm text-gray-600 line-clamp-3">
                        {note.noteContent || 'No content'}
                      </p>
                    </div>

                    {/* Tags */}
                    {note.reminderTags && note.reminderTags.length > 0 && (
                      <div className="flex flex-wrap gap-1 mb-3">
                        {note.reminderTags.slice(0, 3).map((tag, index) => (
                          <span
                            key={index}
                            className="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-medium bg-blue-100 text-blue-700 border border-blue-200"
                          >
                            {tag}
                          </span>
                        ))}
                        {note.reminderTags.length > 3 && (
                          <span className="inline-flex items-center px-2 py-1 rounded-full text-xs bg-gray-100 text-gray-600">
                            +{note.reminderTags.length - 3} more
                          </span>
                        )}
                      </div>
                    )}

                    {/* Note Actions */}
                    <div className="flex items-center justify-between pt-3 border-t border-gray-100">
                      <div className="text-xs text-gray-400">
                        {note.updatedAt && note.updatedAt !== note.createdAt && (
                          <span>Updated {formatDate(note.updatedAt)}</span>
                        )}
                      </div>
                      <button
                        onClick={() => openNoteViewModal(note)}
                        className="inline-flex items-center px-4 py-2.5 text-sm font-medium text-blue-600 hover:text-blue-700 bg-white hover:bg-blue-50 rounded-lg border border-blue-200 hover:border-blue-300 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                      >
                        <FileText className="h-4 w-4 mr-2" />
                        <span>View Content</span>
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Note View Modal */}
      {isViewModalOpen && viewingNote && (
        <div className="fixed inset-0 bg-gray-900 bg-opacity-75 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-7xl max-h-[95vh] overflow-hidden border border-gray-200">
            {/* Modal Header */}
            <div className="flex items-center justify-between px-8 py-6 border-b border-blue-200 bg-gradient-to-r from-blue-50 to-blue-100">
              <div className="flex items-center space-x-4">
                <div className="p-3 bg-blue-100 rounded-xl border border-blue-200">
                  <FileText className="h-7 w-7 text-blue-700" />
                </div>
                <div>
                  <h1 className="text-2xl font-bold text-blue-900 tracking-tight">
                    {viewingNote.noteName || 'Untitled Note'}
                  </h1>
                  <div className="flex items-center space-x-4 mt-1">
                    <span className="flex items-center text-sm text-blue-700 font-medium">
                      <Clock className="h-4 w-4 mr-1.5 text-blue-600" />
                      {viewingNote.createdAt && formatDate(viewingNote.createdAt)}
                    </span>
                    {viewingNote.taskId && (
                      <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold bg-emerald-500 text-white border border-emerald-400 shadow-sm">
                        <span className="h-1.5 w-1.5 bg-white rounded-full mr-2"></span>
                        Task #{viewingNote.taskId}
                      </span>
                    )}
                  </div>
                </div>
              </div>
              <button
                onClick={closeNoteViewModal}
                className="p-2.5 text-blue-700 hover:text-blue-900 hover:bg-blue-200 rounded-xl transition-all duration-200"
              >
                <X className="h-6 w-6" />
              </button>
            </div>

            {/* Modal Content */}
            <div className="flex h-[calc(95vh-140px)]">
              {/* Left Sidebar - Metadata */}
              <div className="w-80 bg-white border-r border-gray-200 p-6 overflow-y-auto">
                <div className="space-y-6">
                  {/* Note Info Card */}
                  <div className="bg-white rounded-lg p-6 border border-gray-200">
                    <div className="flex items-center mb-4">
                      <h3 className="text-lg font-semibold text-gray-900">Note Details</h3>
                    </div>
                    <div className="space-y-4">
                      <div className="border-l-4 border-blue-500 pl-4 py-2">
                        <label className="block text-xs font-medium text-gray-500 uppercase tracking-wide mb-1">Created</label>
                        <p className="text-sm font-medium text-gray-900">
                          {viewingNote.createdAt ? formatDate(viewingNote.createdAt) : 'N/A'}
                        </p>
                      </div>
                      {viewingNote.updatedAt && viewingNote.updatedAt !== viewingNote.createdAt && (
                        <div className="border-l-4 border-emerald-500 pl-4 py-2">
                          <label className="block text-xs font-medium text-gray-500 uppercase tracking-wide mb-1">Last Modified</label>
                          <p className="text-sm font-medium text-gray-900">
                            {formatDate(viewingNote.updatedAt)}
                          </p>
                        </div>
                      )}
                      <div className="border-l-4 border-purple-500 pl-4 py-2">
                        <label className="block text-xs font-medium text-gray-500 uppercase tracking-wide mb-1">Associated Task</label>
                        <p className="text-sm font-medium text-gray-900">
                          {viewingNote.taskId ? `Task #${viewingNote.taskId}` : 'Standalone Note'}
                        </p>
                      </div>
                    </div>
                  </div>

                  {/* Tags Section */}
                  {viewingNote.reminderTags && viewingNote.reminderTags.length > 0 && (
                    <div className="bg-white rounded-lg p-6 border border-gray-200">
                      <div className="flex items-center mb-4">
                        <h3 className="text-lg font-semibold text-gray-900">Tags</h3>
                        <span className="ml-3 inline-flex items-center px-2 py-1 rounded-md text-xs font-medium bg-gray-100 text-gray-600">
                          {viewingNote.reminderTags.length}
                        </span>
                      </div>
                      <div className="flex flex-wrap gap-2">
                        {viewingNote.reminderTags.map((tag, index) => (
                          <span
                            key={index}
                            className="inline-flex items-center px-3 py-1.5 rounded-md text-sm font-medium bg-blue-100 text-blue-700 border border-blue-200"
                          >
                            {tag}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              </div>

              {/* Main Content Area */}
              <div className="flex-1 flex flex-col">
                <div className="flex-1 p-8 overflow-y-auto bg-white">
                  <div className="max-w-4xl mx-auto">
                    <div className="flex items-center mb-6">
                      <h2 className="text-xl font-semibold text-gray-900 mb-1">Note Content</h2>
                      <p className="text-sm text-gray-600">View and manage your note details</p>
                    </div>
                    
                    <div className="bg-white rounded-lg border border-gray-200">
                      <div className="bg-gray-50 px-6 py-4 border-b border-gray-200">
                        <div className="flex items-center">
                          <h3 className="text-lg font-semibold text-gray-900">Content Preview</h3>
                        </div>
                      </div>
                      <div className="p-6">
                        {viewingNote.noteContent ? (
                          <div className="prose max-w-none">
                            <div className="text-gray-800 leading-relaxed whitespace-pre-wrap text-base bg-gray-50 p-6 rounded-lg border border-gray-200">
                              {viewingNote.noteContent}
                            </div>
                          </div>
                        ) : (
                          <div className="text-center py-12">
                            <div className="p-4 bg-gray-100 rounded-full w-16 h-16 mx-auto mb-4 flex items-center justify-center">
                              <FileText className="h-8 w-8 text-gray-400" />
                            </div>
                            <h3 className="text-lg font-semibold text-gray-900 mb-2">No content available</h3>
                            <p className="text-gray-500">This note doesn't have any content yet</p>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Action Bar */}
                <div className="flex items-center justify-between px-8 py-4 bg-gray-50 border-t border-gray-200">
                  <div className="flex items-center space-x-3">
                    <button
                      onClick={() => {
                        selectNoteForEditing(viewingNote);
                        closeNoteViewModal();
                      }}
                      className="inline-flex items-center px-4 py-2.5 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                    >
                      <Edit3 className="h-4 w-4 mr-2" />
                      Edit Note
                    </button>
                    <button
                      onClick={() => {
                        if (viewingNote.id && window.confirm('Are you sure you want to delete this note?')) {
                          handleDeleteNote(viewingNote.id);
                          closeNoteViewModal();
                        }
                      }}
                      className="inline-flex items-center px-4 py-2.5 text-sm font-medium text-red-600 hover:text-red-700 bg-white hover:bg-red-50 border border-red-200 hover:border-red-300 rounded-lg transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
                    >
                      <Trash2 className="h-4 w-4 mr-2" />
                      Delete Note
                    </button>
                  </div>
                  <button
                    onClick={closeNoteViewModal}
                    className="px-4 py-2.5 text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 border border-gray-300 hover:border-gray-400 rounded-lg transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2"
                  >
                    Close
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default NotesPage;