import React, { useState, useEffect } from 'react';
import { X, Save, Trash2, Plus, Tag } from 'lucide-react';
import { taskNoteService } from '../services/taskNoteService';
import type { TaskNoteRequest } from '../types/taskNote';
import { TAG_COLORS, DEFAULT_TAG_COLOR, PREDEFINED_TAGS } from '../types/taskNote';

interface TaskNotesModalProps {
  isOpen: boolean;
  onClose: () => void;
  taskId: number;
  taskTitle: string;
  onNoteUpdated?: () => void;
}

const TaskNotesModal: React.FC<TaskNotesModalProps> = ({
  isOpen,
  onClose,
  taskId,
  taskTitle,
  onNoteUpdated
}) => {
  const [noteContent, setNoteContent] = useState('');
  const [reminderTags, setReminderTags] = useState<string[]>([]);
  const [newTag, setNewTag] = useState('');
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [showTagSuggestions, setShowTagSuggestions] = useState(false);
  const [userTags, setUserTags] = useState<string[]>([]);
  const [hasExistingNote, setHasExistingNote] = useState(false);

  // Load task note and user tags
  useEffect(() => {
    if (isOpen && taskId) {
      loadTaskNote();
      loadUserTags();
    }
  }, [isOpen, taskId]);

  const loadTaskNote = async () => {
    try {
      setLoading(true);
      setError('');
      console.log('Loading task note for task ID:', taskId);
      const note = await taskNoteService.getTaskNote(taskId);
      console.log('Received note response:', note);
      if (note && note.id) {
        setNoteContent(note.noteContent || '');
        setReminderTags(note.reminderTags || []);
        setHasExistingNote(true);
      } else {
        setNoteContent('');
        setReminderTags([]);
        setHasExistingNote(false);
      }
    } catch (err: any) {
      console.error('Failed to load task note:', err);
      console.error('Error response:', err.response);
      // If it's a 404, that's expected for new notes
      if (err.response?.status === 404 || err.message?.includes('not found')) {
        setNoteContent('');
        setReminderTags([]);
        setHasExistingNote(false);
        setError(''); // Don't show error for missing notes
      } else {
        setError('Failed to load note. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  const loadUserTags = async () => {
    try {
      const tags = await taskNoteService.getUserTags();
      setUserTags(tags);
    } catch (err: any) {
      console.warn('Failed to load user tags:', err);
    }
  };

  const handleSave = async () => {
    try {
      setSaving(true);
      setError('');

      const request: TaskNoteRequest = {
        taskId,
        noteContent: noteContent.trim(),
        reminderTags: reminderTags.filter(tag => tag.trim().length > 0)
      };

      console.log('Saving task note with request:', request);
      await taskNoteService.saveTaskNote(request);
      console.log('Task note saved successfully');
      setHasExistingNote(true);
      onNoteUpdated?.();
      onClose();
    } catch (err: any) {
      console.error('Failed to save task note:', err);
      console.error('Error response:', err.response);
      setError('Failed to save note. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!hasExistingNote) return;
    
    if (!confirm('Are you sure you want to delete this note? This action cannot be undone.')) {
      return;
    }

    try {
      setSaving(true);
      setError('');
      await taskNoteService.deleteTaskNote(taskId);
      setNoteContent('');
      setReminderTags([]);
      setHasExistingNote(false);
      onNoteUpdated?.();
      onClose();
    } catch (err: any) {
      console.error('Failed to delete task note:', err);
      setError('Failed to delete note. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  const addTag = (tag: string) => {
    const trimmedTag = tag.trim().toLowerCase();
    if (trimmedTag && !reminderTags.includes(trimmedTag)) {
      setReminderTags([...reminderTags, trimmedTag]);
      setNewTag('');
      setShowTagSuggestions(false);
    }
  };

  const removeTag = (tagToRemove: string) => {
    setReminderTags(reminderTags.filter(tag => tag !== tagToRemove));
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && newTag.trim()) {
      e.preventDefault();
      addTag(newTag);
    }
  };

  const getTagColor = (tag: string): string => {
    return TAG_COLORS[tag] || DEFAULT_TAG_COLOR;
  };

  const getSuggestedTags = (): string[] => {
    const input = newTag.toLowerCase();
    const allTags = [...new Set([...PREDEFINED_TAGS, ...userTags])];
    
    return allTags
      .filter(tag => 
        !reminderTags.includes(tag) && 
        tag.toLowerCase().includes(input)
      )
      .slice(0, 8);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <div>
            <h2 className="text-xl font-semibold text-gray-900">Personal Notes</h2>
            <p className="text-sm text-gray-600 mt-1">{taskTitle}</p>
          </div>
          <button
            onClick={onClose}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <X className="h-5 w-5 text-gray-500" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 overflow-y-auto max-h-[calc(90vh-180px)]">
          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded-lg text-sm">
              {error}
            </div>
          )}

          {loading ? (
            <div className="flex items-center justify-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            </div>
          ) : (
            <div className="space-y-6">
              {/* Note Content */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Your Notes
                </label>
                <textarea
                  value={noteContent}
                  onChange={(e) => setNoteContent(e.target.value)}
                  placeholder="Add your personal notes about this task here..."
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
                  rows={6}
                />
              </div>

              {/* Reminder Tags */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Reminder Tags
                </label>
                
                {/* Existing Tags */}
                {reminderTags.length > 0 && (
                  <div className="flex flex-wrap gap-2 mb-3">
                    {reminderTags.map((tag, index) => (
                      <span
                        key={index}
                        className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium ${getTagColor(tag)}`}
                      >
                        <Tag className="h-3 w-3 mr-1" />
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
                )}

                {/* Add New Tag */}
                <div className="relative">
                  <div className="flex gap-2">
                    <input
                      type="text"
                      value={newTag}
                      onChange={(e) => {
                        setNewTag(e.target.value);
                        setShowTagSuggestions(e.target.value.length > 0);
                      }}
                      onKeyPress={handleKeyPress}
                      placeholder="Add a reminder tag..."
                      className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                    />
                    <button
                      onClick={() => addTag(newTag)}
                      disabled={!newTag.trim()}
                      className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed text-sm font-medium"
                    >
                      <Plus className="h-4 w-4" />
                    </button>
                  </div>

                  {/* Tag Suggestions */}
                  {showTagSuggestions && getSuggestedTags().length > 0 && (
                    <div className="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-200 rounded-lg shadow-lg z-10 max-h-40 overflow-y-auto">
                      {getSuggestedTags().map((tag, index) => (
                        <button
                          key={index}
                          onClick={() => addTag(tag)}
                          className="w-full text-left px-3 py-2 hover:bg-gray-50 text-sm border-b last:border-b-0 border-gray-100"
                        >
                          <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${getTagColor(tag)}`}>
                            <Tag className="h-3 w-3 mr-1" />
                            {tag}
                          </span>
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="flex items-center justify-between p-6 border-t border-gray-200 bg-gray-50">
          <div>
            {hasExistingNote && (
              <button
                onClick={handleDelete}
                disabled={saving}
                className="inline-flex items-center px-4 py-2 text-sm font-medium text-red-700 bg-red-50 border border-red-200 rounded-lg hover:bg-red-100 disabled:opacity-50"
              >
                <Trash2 className="h-4 w-4 mr-2" />
                Delete Note
              </button>
            )}
          </div>
          
          <div className="flex gap-3">
            <button
              onClick={onClose}
              disabled={saving}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              onClick={handleSave}
              disabled={saving || (!noteContent.trim() && reminderTags.length === 0)}
              className="inline-flex items-center px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:opacity-50"
            >
              {saving ? (
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
              ) : (
                <Save className="h-4 w-4 mr-2" />
              )}
              Save Note
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TaskNotesModal;