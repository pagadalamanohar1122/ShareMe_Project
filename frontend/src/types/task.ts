export interface Task {
  id: number;
  title: string;
  description?: string;
  status: TaskStatus;
  priority: TaskPriority;
  project: ProjectInfo;
  assignee?: UserInfo;
  creator: UserInfo;
  createdAt: string;
  updatedAt?: string;
  dueDate?: string;
  attachments?: TaskAttachment[];
}

export interface TaskRequest {
  title: string;
  description?: string;
  status?: TaskStatus;
  priority?: TaskPriority;
  projectId: number;
  assigneeId?: number;
  dueDate?: string;
}

export interface TaskSearchRequest {
  query?: string;
  status?: TaskStatus;
  priority?: TaskPriority;
  projectId?: number;
  assigneeId?: number;
  creatorId?: number;
  sortBy?: TaskSortField;
  sortDirection?: SortDirection;
  page?: number;
  size?: number;
}

export interface TaskResponse {
  content: Task[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface TaskStats {
  totalTasks: number;
  todoTasks: number;
  inProgressTasks: number;
  completedTasks: number;
}

export interface ProjectInfo {
  id: number;
  name: string;
  status: string;
}

export interface UserInfo {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
}

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export type TaskSortField = 'createdAt' | 'updatedAt' | 'dueDate' | 'title' | 'priority';

export type SortDirection = 'ASC' | 'DESC';

export const TASK_STATUS_OPTIONS: { value: TaskStatus; label: string; color: string }[] = [
  { value: 'TODO', label: 'To Do', color: 'bg-gray-100 text-gray-800' },
  { value: 'IN_PROGRESS', label: 'In Progress', color: 'bg-blue-100 text-blue-800' },
  { value: 'COMPLETED', label: 'Completed', color: 'bg-green-100 text-green-800' },
  { value: 'CANCELLED', label: 'Cancelled', color: 'bg-red-100 text-red-800' },
];

export const TASK_PRIORITY_OPTIONS: { value: TaskPriority; label: string; color: string }[] = [
  { value: 'LOW', label: 'Low', color: 'bg-green-100 text-green-800' },
  { value: 'MEDIUM', label: 'Medium', color: 'bg-yellow-100 text-yellow-800' },
  { value: 'HIGH', label: 'High', color: 'bg-orange-100 text-orange-800' },
  { value: 'URGENT', label: 'Urgent', color: 'bg-red-100 text-red-800' },
];

export const TASK_SORT_OPTIONS: { value: TaskSortField; label: string }[] = [
  { value: 'createdAt', label: 'Created Date' },
  { value: 'updatedAt', label: 'Updated Date' },
  { value: 'dueDate', label: 'Due Date' },
  { value: 'title', label: 'Title' },
  { value: 'priority', label: 'Priority' },
];

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