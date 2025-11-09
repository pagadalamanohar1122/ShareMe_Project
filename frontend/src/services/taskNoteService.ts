import api from './api';
import type { TaskNoteRequest, TaskNoteResponse } from '../types/taskNote';

// Simple local storage implementation for testing
class LocalTaskNoteService {
  private storageKey = 'task_notes';

  private getNotesFromStorage(): TaskNoteResponse[] {
    try {
      const stored = localStorage.getItem(this.storageKey);
      return stored ? JSON.parse(stored) : [];
    } catch {
      return [];
    }
  }

  private saveNotesToStorage(notes: TaskNoteResponse[]): void {
    try {
      localStorage.setItem(this.storageKey, JSON.stringify(notes));
    } catch (error) {
      console.error('Failed to save notes to localStorage:', error);
    }
  }

  async saveTaskNote(request: TaskNoteRequest): Promise<TaskNoteResponse> {
    const notes = this.getNotesFromStorage();
    const now = new Date().toISOString();
    
    const newNote: TaskNoteResponse = {
      id: Date.now(), // Simple ID generation
      taskId: request.taskId,
      taskTitle: null,
      noteName: request.noteName,
      noteContent: request.noteContent,
      reminderTags: request.reminderTags || [],
      createdAt: now,
      updatedAt: now
    };

    notes.unshift(newNote); // Add to beginning
    this.saveNotesToStorage(notes);
    
    console.log('Note saved to localStorage:', newNote);
    return newNote;
  }

  async getAllUserNotes(): Promise<TaskNoteResponse[]> {
    const notes = this.getNotesFromStorage();
    console.log('Retrieved notes from localStorage:', notes);
    return notes;
  }

  async getUserTags(): Promise<string[]> {
    const notes = this.getNotesFromStorage();
    const tagSet = new Set<string>();
    
    notes.forEach(note => {
      note.reminderTags?.forEach(tag => tagSet.add(tag));
    });
    
    return Array.from(tagSet);
  }

  async deleteNote(noteId: number): Promise<void> {
    const notes = this.getNotesFromStorage();
    const filteredNotes = notes.filter(note => note.id !== noteId);
    this.saveNotesToStorage(filteredNotes);
  }
}

// Use localStorage implementation for now
const localService = new LocalTaskNoteService();

export const taskNoteService = {
  /**
   * Get personal note for a specific task
   */
  async getTaskNote(taskId: number): Promise<TaskNoteResponse> {
    console.log('API: Getting task note for task ID:', taskId);
    try {
      const response = await api.get<TaskNoteResponse>(`/api/task-notes/task/${taskId}`);
      console.log('API: Task note response:', response.data);
      return response.data;
    } catch (error) {
      console.log('Falling back to localStorage for getTaskNote');
      const notes = await localService.getAllUserNotes();
      const note = notes.find(n => n.taskId === taskId);
      if (note) return note;
      
      // Return empty note
      return {
        id: 0,
        taskId,
        taskTitle: null,
        noteName: undefined,
        noteContent: '',
        reminderTags: [],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
    }
  },

  /**
   * Save (create or update) personal note for a task
   */
  async saveTaskNote(request: TaskNoteRequest): Promise<TaskNoteResponse> {
    console.log('API: Saving task note with request:', request);
    try {
      const response = await api.post<TaskNoteResponse>('/api/task-notes', request);
      console.log('API: Save task note response:', response.data);
      return response.data;
    } catch (error) {
      console.log('Falling back to localStorage for saveTaskNote');
      return await localService.saveTaskNote(request);
    }
  },

  /**
   * Delete personal note for a task
   */
  async deleteTaskNote(taskId: number): Promise<void> {
    try {
      await api.delete(`/api/task-notes/task/${taskId}`);
    } catch (error) {
      console.log('Falling back to localStorage for deleteTaskNote');
      const notes = await localService.getAllUserNotes();
      const note = notes.find(n => n.taskId === taskId);
      if (note) {
        await localService.deleteNote(note.id);
      }
    }
  },

  /**
   * Get all personal task notes for the current user
   */
  async getAllTaskNotes(): Promise<TaskNoteResponse[]> {
    try {
      const response = await api.get<TaskNoteResponse[]>('/api/task-notes');
      return response.data;
    } catch (error) {
      console.log('Falling back to localStorage for getAllTaskNotes');
      return await localService.getAllUserNotes();
    }
  },

  /**
   * Get all personal notes for the current user (alias for getAllTaskNotes)
   */
  async getAllUserNotes(): Promise<TaskNoteResponse[]> {
    return this.getAllTaskNotes();
  },

  /**
   * Get task notes filtered by a specific reminder tag
   */
  async getTaskNotesByTag(tag: string): Promise<TaskNoteResponse[]> {
    try {
      const response = await api.get<TaskNoteResponse[]>(`/api/task-notes/tag/${encodeURIComponent(tag)}`);
      return response.data;
    } catch (error) {
      console.log('Falling back to localStorage for getTaskNotesByTag');
      const notes = await localService.getAllUserNotes();
      return notes.filter(note => note.reminderTags?.includes(tag));
    }
  },

  /**
   * Get all unique reminder tags used by the current user
   */
  async getUserTags(): Promise<string[]> {
    try {
      const response = await api.get<string[]>('/api/task-notes/tags');
      return response.data;
    } catch (error) {
      console.log('Falling back to localStorage for getUserTags');
      return await localService.getUserTags();
    }
  },

  /**
   * Check if the current user has a personal note for a specific task
   */
  async hasTaskNote(taskId: number): Promise<boolean> {
    try {
      const response = await api.get<boolean>(`/api/task-notes/task/${taskId}/exists`);
      return response.data;
    } catch (error) {
      console.log('Falling back to localStorage for hasTaskNote');
      const notes = await localService.getAllUserNotes();
      return notes.some(note => note.taskId === taskId);
    }
  },

  /**
   * Batch check if user has notes for multiple tasks
   */
  async hasTaskNotes(taskIds: number[]): Promise<Record<number, boolean>> {
    const checks = await Promise.all(
      taskIds.map(async (taskId) => {
        try {
          const hasNote = await this.hasTaskNote(taskId);
          return { taskId, hasNote };
        } catch (error) {
          console.warn(`Error checking note for task ${taskId}, defaulting to false:`, error);
          return { taskId, hasNote: false };
        }
      })
    );

    // Convert all results to a record, no filtering needed since we always return a result
    return checks.reduce((acc, { taskId, hasNote }) => {
      acc[taskId] = hasNote;
      return acc;
    }, {} as Record<number, boolean>);
  }
};