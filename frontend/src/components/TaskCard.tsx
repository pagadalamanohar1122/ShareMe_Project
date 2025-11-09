import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FileText } from 'lucide-react';
import type { Task } from '../types/task';
import { TASK_STATUS_OPTIONS, TASK_PRIORITY_OPTIONS } from '../types/task';
import AttachmentList from './AttachmentList';
import { taskNoteService } from '../services/taskNoteService';

interface TaskCardProps {
  task: Task;
  onStatusChange?: (taskId: number, newStatus: string) => void;
  onEdit?: (task: Task) => void;
  onDelete?: (taskId: number) => void;
  isDarkMode?: boolean;
}

const TaskCard: React.FC<TaskCardProps> = ({ 
  task, 
  onStatusChange, 
  onEdit, 
  onDelete, 
  isDarkMode = false 
}) => {
  const navigate = useNavigate();
  const [hasPersonalNote, setHasPersonalNote] = useState(false);
  
  const statusOption = TASK_STATUS_OPTIONS.find(opt => opt.value === task.status);
  const priorityOption = TASK_PRIORITY_OPTIONS.find(opt => opt.value === task.priority);

  // Check if user has personal notes for this task
  useEffect(() => {
    checkForPersonalNote();
  }, [task.id]);

  const checkForPersonalNote = async () => {
    try {
      const hasNote = await taskNoteService.hasTaskNote(task.id);
      setHasPersonalNote(hasNote);
    } catch (error) {
      console.warn('Failed to check for personal note:', error);
    }
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return null;
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const isOverdue = task.dueDate && new Date(task.dueDate) < new Date() && task.status !== 'COMPLETED';

  const handleStatusChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    e.stopPropagation();
    if (onStatusChange) {
      onStatusChange(task.id, e.target.value);
    }
  };

  const handleEdit = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (onEdit) {
      onEdit(task);
    }
  };

  const handleDelete = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (onDelete && window.confirm('Are you sure you want to delete this task?')) {
      onDelete(task.id);
    }
  };

  const handleNotesClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    navigate(`/notes?taskId=${task.id}`);
  };

  return (
    <div className={`rounded-lg border p-4 transition-all duration-200 hover:shadow-md cursor-pointer ${
      isDarkMode 
        ? 'bg-gray-800 border-gray-700 hover:bg-gray-750' 
        : 'bg-white border-gray-200 hover:bg-gray-50'
    } ${isOverdue ? 'border-red-300 bg-red-50' : ''}`}>
      
      {/* Header with title and actions */}
      <div className="flex items-start justify-between mb-3">
        <div className="flex-1 min-w-0">
          <h3 className={`text-sm font-semibold truncate ${
            isDarkMode ? 'text-white' : 'text-gray-900'
          } ${isOverdue ? 'text-red-900' : ''}`}>
            {task.title}
          </h3>
          <p className={`text-xs mt-1 ${
            isDarkMode ? 'text-gray-400' : 'text-gray-500'
          }`}>
            {task.project.name}
          </p>
        </div>
        
        <div className="flex items-center space-x-1 ml-2">
          {/* Personal Notes Button */}
          <button
            onClick={handleNotesClick}
            className={`p-1 rounded transition-colors relative ${
              isDarkMode 
                ? 'hover:bg-gray-700 text-gray-400 hover:text-blue-400' 
                : 'hover:bg-gray-100 text-gray-500 hover:text-blue-600'
            }`}
            title={hasPersonalNote ? "View personal notes" : "Add personal notes"}
          >
            <FileText className="w-4 h-4" />
            {hasPersonalNote && (
              <div className="absolute -top-1 -right-1 w-2 h-2 bg-blue-500 rounded-full"></div>
            )}
          </button>
          
          {onEdit && (
            <button
              onClick={handleEdit}
              className={`p-1 rounded transition-colors ${
                isDarkMode 
                  ? 'hover:bg-gray-700 text-gray-400 hover:text-blue-400' 
                  : 'hover:bg-gray-100 text-gray-500 hover:text-blue-600'
              }`}
              title="Edit task"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
            </button>
          )}
          
          {onDelete && (
            <button
              onClick={handleDelete}
              className={`p-1 rounded transition-colors ${
                isDarkMode 
                  ? 'hover:bg-gray-700 text-gray-400 hover:text-red-400' 
                  : 'hover:bg-gray-100 text-gray-500 hover:text-red-600'
              }`}
              title="Delete task"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          )}
        </div>
      </div>

      {/* Description */}
      {task.description && (
        <p className={`text-xs mb-3 line-clamp-2 ${
          isDarkMode ? 'text-gray-300' : 'text-gray-600'
        }`}>
          {task.description}
        </p>
      )}

      {/* Attachments */}
      {task.attachments && task.attachments.length > 0 && (
        <AttachmentList
          attachments={task.attachments}
          isDarkMode={isDarkMode}
          maxDisplay={2}
        />
      )}

      {/* Status and Priority badges */}
      <div className="flex items-center space-x-2 mb-3">
        {statusOption && (
          <span className={`px-2 py-1 text-xs font-medium rounded-full ${
            isDarkMode 
              ? 'bg-gray-700 text-gray-200' 
              : statusOption.color
          }`}>
            {statusOption.label}
          </span>
        )}
        
        {priorityOption && (
          <span className={`px-2 py-1 text-xs font-medium rounded-full ${
            isDarkMode 
              ? 'bg-gray-700 text-gray-200' 
              : priorityOption.color
          }`}>
            {priorityOption.label}
          </span>
        )}
      </div>

      {/* Due date and assignee */}
      <div className="space-y-2">
        {task.dueDate && (
          <div className="flex items-center space-x-1">
            <svg className={`w-3 h-3 ${
              isOverdue ? 'text-red-500' : isDarkMode ? 'text-gray-400' : 'text-gray-500'
            }`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            <span className={`text-xs ${
              isOverdue 
                ? 'text-red-600 font-medium' 
                : isDarkMode ? 'text-gray-300' : 'text-gray-600'
            }`}>
              Due: {formatDate(task.dueDate)}
              {isOverdue && ' (Overdue)'}
            </span>
          </div>
        )}

        {task.assignee && (
          <div className="flex items-center space-x-1">
            <div className="w-5 h-5 bg-blue-500 rounded-full flex items-center justify-center text-white text-xs font-semibold">
              {task.assignee.firstName.charAt(0)}{task.assignee.lastName.charAt(0)}
            </div>
            <span className={`text-xs ${
              isDarkMode ? 'text-gray-300' : 'text-gray-600'
            }`}>
              {task.assignee.firstName} {task.assignee.lastName}
            </span>
          </div>
        )}
      </div>

      {/* Status change dropdown */}
      {onStatusChange && (
        <div className="mt-3 pt-3 border-t border-gray-200 dark:border-gray-700">
          <select
            value={task.status}
            onChange={handleStatusChange}
            className={`w-full text-xs rounded border p-1 ${
              isDarkMode 
                ? 'bg-gray-700 border-gray-600 text-white' 
                : 'bg-white border-gray-300 text-gray-900'
            }`}
            onClick={(e) => e.stopPropagation()}
          >
            {TASK_STATUS_OPTIONS.map(option => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
      )}

      {/* Created date */}
      <div className={`text-xs mt-2 pt-2 border-t ${
        isDarkMode ? 'border-gray-700 text-gray-400' : 'border-gray-100 text-gray-400'
      }`}>
        Created: {formatDate(task.createdAt)}
      </div>
    </div>
  );
};

export default TaskCard;