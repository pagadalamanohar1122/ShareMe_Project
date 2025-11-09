import React from 'react';
import type { TaskAttachment } from '../types/task';
import { taskAttachmentService } from '../services/attachmentService';

interface AttachmentListProps {
  attachments: TaskAttachment[];
  onDelete?: (attachmentId: number) => void;
  isDarkMode?: boolean;
  maxDisplay?: number;
}

const AttachmentList: React.FC<AttachmentListProps> = ({
  attachments,
  onDelete,
  isDarkMode = false,
  maxDisplay = 3
}) => {
  if (!attachments || attachments.length === 0) {
    return null;
  }

  const displayAttachments = attachments.slice(0, maxDisplay);
  const remainingCount = attachments.length - maxDisplay;

  const handleDownload = (attachment: TaskAttachment) => {
    taskAttachmentService.downloadAttachmentAsFile(attachment);
  };

  const getFileIcon = (contentType: string) => {
    if (contentType.startsWith('image/')) {
      return (
        <svg className="h-4 w-4 text-blue-500" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z" clipRule="evenodd" />
        </svg>
      );
    } else if (contentType.startsWith('video/')) {
      return (
        <svg className="h-4 w-4 text-purple-500" fill="currentColor" viewBox="0 0 20 20">
          <path d="M2 6a2 2 0 012-2h6a2 2 0 012 2v8a2 2 0 01-2 2H4a2 2 0 01-2-2V6zM14.553 7.106A1 1 0 0014 8v4a1 1 0 00.553.894l2 1A1 1 0 0018 13V7a1 1 0 00-1.447-.894l-2 1z" />
        </svg>
      );
    } else if (contentType === 'application/pdf') {
      return (
        <svg className="h-4 w-4 text-red-500" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z" clipRule="evenodd" />
        </svg>
      );
    } else {
      return (
        <svg className="h-4 w-4 text-gray-500" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z" clipRule="evenodd" />
        </svg>
      );
    }
  };

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  };

  return (
    <div className="mt-3">
      <div className={`text-xs font-medium mb-2 ${
        isDarkMode ? 'text-gray-300' : 'text-gray-600'
      }`}>
        Attachments ({attachments.length})
      </div>
      
      <div className="space-y-1">
        {displayAttachments.map((attachment) => (
          <div
            key={attachment.id}
            className={`flex items-center justify-between p-2 rounded border ${
              isDarkMode 
                ? 'bg-gray-700 border-gray-600 hover:bg-gray-600' 
                : 'bg-gray-50 border-gray-200 hover:bg-gray-100'
            } transition-colors cursor-pointer group`}
            onClick={() => handleDownload(attachment)}
          >
            <div className="flex items-center space-x-2 flex-1 min-w-0">
              {getFileIcon(attachment.contentType)}
              <div className="flex-1 min-w-0">
                <p className={`text-xs truncate ${
                  isDarkMode ? 'text-gray-200' : 'text-gray-700'
                }`}>
                  {attachment.originalFilename}
                </p>
                <p className={`text-xs ${
                  isDarkMode ? 'text-gray-400' : 'text-gray-500'
                }`}>
                  {formatFileSize(attachment.fileSize)}
                </p>
              </div>
            </div>
            
            <div className="flex items-center space-x-1 opacity-0 group-hover:opacity-100 transition-opacity">
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  handleDownload(attachment);
                }}
                className={`p-1 rounded hover:bg-blue-100 ${
                  isDarkMode ? 'text-gray-300 hover:text-blue-400' : 'text-gray-500 hover:text-blue-600'
                }`}
                title="Download"
              >
                <svg className="h-3 w-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </button>
              
              {onDelete && (
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onDelete(attachment.id);
                  }}
                  className={`p-1 rounded hover:bg-red-100 ${
                    isDarkMode ? 'text-gray-300 hover:text-red-400' : 'text-gray-500 hover:text-red-600'
                  }`}
                  title="Delete"
                >
                  <svg className="h-3 w-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              )}
            </div>
          </div>
        ))}
        
        {remainingCount > 0 && (
          <div className={`text-xs text-center py-1 ${
            isDarkMode ? 'text-gray-400' : 'text-gray-500'
          }`}>
            +{remainingCount} more attachment{remainingCount > 1 ? 's' : ''}
          </div>
        )}
      </div>
    </div>
  );
};

export default AttachmentList;