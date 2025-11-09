import api from './api';

export interface TaskAttachment {
  id: number;
  originalFilename: string;
  fileSize: number;
  contentType: string;
  downloadUrl: string;
  uploader: {
    id: number;
    username: string;
    email: string;
    firstName: string;
    lastName: string;
  };
  uploadedAt: string;
}

export interface AttachmentStats {
  attachmentCount: number;
  totalFileSize: number;
  totalFileSizeFormatted: string;
}

class TaskAttachmentService {
  /**
   * Upload files to a task
   */
  async uploadFiles(taskId: number, files: File[]): Promise<TaskAttachment[]> {
    const formData = new FormData();
    files.forEach(file => {
      formData.append('files', file);
    });

    const response = await api.post(`/tasks/${taskId}/attachments`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    return response.data;
  }

  /**
   * Upload a single file to a task
   */
  async uploadSingleFile(taskId: number, file: File): Promise<TaskAttachment> {
    const formData = new FormData();
    formData.append('file', file);

    const response = await api.post(`/tasks/${taskId}/attachments/single`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    return response.data;
  }

  /**
   * Get all attachments for a task
   */
  async getTaskAttachments(taskId: number): Promise<TaskAttachment[]> {
    const response = await api.get(`/tasks/${taskId}/attachments`);
    return response.data;
  }

  /**
   * Download an attachment
   */
  async downloadAttachment(attachmentId: number): Promise<Blob> {
    const response = await api.get(`/tasks/attachments/${attachmentId}/download`, {
      responseType: 'blob',
    });
    return response.data;
  }

  /**
   * Delete an attachment
   */
  async deleteAttachment(attachmentId: number): Promise<void> {
    await api.delete(`/tasks/attachments/${attachmentId}`);
  }

  /**
   * Get attachment statistics for a task
   */
  async getAttachmentStats(taskId: number): Promise<AttachmentStats> {
    const response = await api.get(`/tasks/${taskId}/attachments/stats`);
    return response.data;
  }

  /**
   * Download attachment with filename
   */
  downloadAttachmentAsFile(attachment: TaskAttachment): Promise<void> {
    return this.downloadAttachment(attachment.id).then(blob => {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = attachment.originalFilename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    });
  }

  /**
   * Format file size for display
   */
  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  /**
   * Get file type icon class based on content type
   */
  getFileTypeIcon(contentType: string): string {
    if (contentType.startsWith('image/')) {
      return 'fas fa-image';
    } else if (contentType.startsWith('video/')) {
      return 'fas fa-video';
    } else if (contentType === 'application/pdf') {
      return 'fas fa-file-pdf';
    } else if (contentType.includes('word') || contentType.includes('document')) {
      return 'fas fa-file-word';
    } else if (contentType.includes('sheet') || contentType.includes('excel')) {
      return 'fas fa-file-excel';
    } else if (contentType.includes('presentation') || contentType.includes('powerpoint')) {
      return 'fas fa-file-powerpoint';
    } else if (contentType === 'text/plain') {
      return 'fas fa-file-alt';
    } else {
      return 'fas fa-file';
    }
  }

  /**
   * Validate file before upload
   */
  validateFile(file: File, maxSize: number = 50): { isValid: boolean; error?: string } {
    // Check file size (in MB)
    if (file.size > maxSize * 1024 * 1024) {
      return {
        isValid: false,
        error: `File size exceeds ${maxSize}MB limit`
      };
    }

    // Check file type
    const allowedTypes = [
      'image/jpeg', 'image/png', 'image/gif', 'image/bmp', 'image/svg+xml',
      'video/mp4', 'video/avi', 'video/mov', 'video/wmv', 'video/flv', 'video/webm',
      'application/pdf',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      'application/vnd.ms-excel',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      'application/vnd.ms-powerpoint',
      'application/vnd.openxmlformats-officedocument.presentationml.presentation',
      'text/plain'
    ];

    if (!allowedTypes.includes(file.type)) {
      return {
        isValid: false,
        error: 'File type not allowed'
      };
    }

    return { isValid: true };
  }
}

export const taskAttachmentService = new TaskAttachmentService();