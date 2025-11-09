import axios from 'axios';
import type { 
  AuthResponse, 
  LoginRequest, 
  SignupRequest, 
  ForgotPasswordRequest, 
  ResetPasswordRequest,
  User 
} from '../types/auth';
import type {
  Task,
  TaskRequest,
  TaskSearchRequest,
  TaskResponse,
  TaskStats
} from '../types/task';

export interface Project {
  id: number;
  name: string;
  description?: string;
  status: 'ACTIVE' | 'COMPLETED' | 'ON_HOLD' | 'ARCHIVED';
  priority: 'URGENT' | 'HIGH' | 'MEDIUM' | 'LOW';
  owner: User;
  members: User[];
  documents: ProjectDocument[];
  totalDocuments: number;
  totalTasks: number;
  completedTasks: number;
  inProgressTasks: number;
  createdAt: string;
  updatedAt?: string;
  deadline?: string;
  // Permissions returned by backend
  currentUserRole?: string;
  canView?: boolean;
  canEdit?: boolean;
  canDelete?: boolean;
}

export interface CreateProjectRequest {
  name: string;
  description?: string;
  priority: 'URGENT' | 'HIGH' | 'MEDIUM' | 'LOW';
  deadline?: string;
  memberEmails: string[];
  documents: File[];
  sendEmail: boolean;
}

export interface DashboardStats {
  totalProjects: number;
  completedTasks: number;
  inProgressTasks: number;
}

const API_BASE_URL = 'http://localhost:8080/api';  // Use correct backend URL

console.log('API Base URL:', API_BASE_URL); // Debug log

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // Increased timeout to 10 seconds
  withCredentials: false // Set to false since we're using token auth
});

// Add token to requests if available
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  console.log('API Request:', config.method?.toUpperCase(), config.url);
  console.log('Token available:', !!token);
  if (!config.headers) {
    config.headers = {} as any;
  }
  if (token) {
    (config.headers as any).Authorization = `Bearer ${token}`;
    // Ensure content type is set correctly for non-form-data requests
    const ct = (config.headers as any)['Content-Type'];
    if (!ct || (typeof ct === 'string' && !ct.includes('multipart/form-data'))) {
      (config.headers as any)['Content-Type'] = 'application/json';
    }
  }
  return config;
}, (error: any) => {
  console.error('Request interceptor error:', error);
  return Promise.reject(error);
});

// Handle token expiry
api.interceptors.response.use(
  (response) => {
    console.log('API Response:', response.status, response.config.url);
    return response;
  },
  async (error: any) => {
    console.log('API Error Response:', error?.response?.status, error?.config?.url, error?.message);
    
    // Handle 401 Unauthorized - redirect to login
    if (error?.response?.status === 401 && !error?.config?.url?.includes('/api/auth/login')) {
      localStorage.removeItem('token');
      window.location.href = '/login';
      return Promise.reject(error);
    }

    // For task note existence checks, handle 404 by returning false
    if (error?.config?.url?.includes('/task-notes/task/') && error?.config?.url?.includes('/exists')) {
      console.log('Task note check returned error, defaulting to false');
      return { data: false } as any; // mimic axios response
    }

    // For task notes endpoints, return empty/default responses instead of errors
    if (error?.config?.url?.includes('/task-notes/')) {
      if (error?.config?.method === 'get') {
        console.log('Task note GET error, returning empty response');
        return {
          data: {
            id: null,
            taskId: null,
            taskTitle: null,
            noteName: null,
            noteContent: '',
            reminderTags: [],
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString()
          }
        } as any;
      }
      if (error?.config?.method === 'delete') {
        console.log('Task note DELETE error, returning success');
        return { data: null } as any;
      }
    }

    return Promise.reject(error);
  }
);

export const authService = {
  async signup(data: SignupRequest): Promise<void> {
    await api.post('/auth/signup', data);
  },

  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/login', data);
    return response.data;
  },

  async getCurrentUser(): Promise<User> {
    const response = await api.get<User>('/auth/me');
    return response.data;
  },

  async forgotPassword(data: ForgotPasswordRequest): Promise<void> {
    await api.post('/auth/forgot', data);
  },

  async resetPassword(data: ResetPasswordRequest): Promise<void> {
    await api.post('/auth/reset', data);
  },
};

export interface ProjectDocument {
  id: number;
  name: string;
  url: string;
  createdAt: string;
}

export const projectService = {
  async searchUsers(query: string): Promise<User[]> {
    const response = await api.get<User[]>(`/users/search?q=${encodeURIComponent(query)}`);
    return response.data;
  },

  async getUserProjects(): Promise<Project[]> {
    const token = localStorage.getItem('token');
    if (!token) {
      throw new Error('No authentication token found');
    }
    const response = await api.get<Project[]>('/projects', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    return response.data;
  },

  async searchProjects(query: string): Promise<Project[]> {
    const response = await api.get<Project[]>(`/projects/search?q=${encodeURIComponent(query)}`);
    return response.data;
  },

  async getProjects(): Promise<Project[]> {
    const response = await api.get<Project[]>('/projects');
    return response.data;
  },

  async createProject(data: CreateProjectRequest): Promise<Project> {
    try {
      // First, create the project without documents
      const projectData = {
        name: data.name,
        description: data.description,
        priority: data.priority,
        deadline: data.deadline,
        memberEmails: data.memberEmails,
        sendEmail: data.sendEmail
      };

      let createdProject = (await api.post<Project>('/projects', projectData)).data;
      console.log('Project created successfully:', createdProject);

      // Then, upload documents one by one if any
      if (data.documents && data.documents.length > 0) {
        for (const file of data.documents) {
          console.log('Uploading document:', file.name);
          const docFormData = new FormData();
          docFormData.append('file', file);
            try {
              const docResponse = await api.post<Project>(
                `/projects/${createdProject.id}/documents`,
                docFormData,
                {
                  headers: {
                    'Content-Type': 'multipart/form-data'
                  }
                }
              );
              createdProject = docResponse.data; // Update with latest document info
              console.log('Document uploaded successfully:', file.name);
            } catch (err: any) {
              console.error('Error uploading document:', file.name, err);
              const message = typeof err?.message === 'string' ? err.message : 'Unknown error';
              throw new Error(`Failed to upload document ${file.name}: ${message}`);
            }
        }
      }

      return createdProject;
    } catch (err) {
      console.error('Error in createProject:', err);
      throw err;
    }
  },

  async getProject(id: number): Promise<Project> {
    const response = await api.get<Project>(`/projects/${id}`);
    return response.data;
  },

  async updateProject(id: number, data: CreateProjectRequest): Promise<Project> {
    const response = await api.put<Project>(`/projects/${id}`, data);
    return response.data;
  },

  async deleteProject(id: number): Promise<void> {
    await api.delete(`/projects/${id}`);
  },

  async getStats(): Promise<DashboardStats> {
    const response = await api.get<DashboardStats>('/projects/stats');
    return response.data;
  },
};

export const taskService = {
  async getTasks(searchParams?: TaskSearchRequest): Promise<TaskResponse> {
    const params = new URLSearchParams();
    
    if (searchParams?.query) params.append('query', searchParams.query);
    if (searchParams?.status) params.append('status', searchParams.status);
    if (searchParams?.priority) params.append('priority', searchParams.priority);
    if (searchParams?.projectId) params.append('projectId', searchParams.projectId.toString());
    if (searchParams?.assigneeId) params.append('assigneeId', searchParams.assigneeId.toString());
    if (searchParams?.creatorId) params.append('creatorId', searchParams.creatorId.toString());
    if (searchParams?.sortBy) params.append('sortBy', searchParams.sortBy);
    if (searchParams?.sortDirection) params.append('sortDirection', searchParams.sortDirection);
    if (searchParams?.page !== undefined) params.append('page', searchParams.page.toString());
    if (searchParams?.size !== undefined) params.append('size', searchParams.size.toString());
    
    const response = await api.get<TaskResponse>(`/tasks?${params.toString()}`);
    return response.data;
  },

  async getTask(id: number): Promise<Task> {
    const response = await api.get<Task>(`/tasks/${id}`);
    return response.data;
  },

  async createTask(data: TaskRequest): Promise<Task> {
    const response = await api.post<Task>('/tasks', data);
    return response.data;
  },

  async updateTask(id: number, data: TaskRequest): Promise<Task> {
    const response = await api.put<Task>(`/tasks/${id}`, data);
    return response.data;
  },

  async updateTaskStatus(id: number, status: string): Promise<Task> {
    const response = await api.patch<Task>(`/tasks/${id}/status`, { status });
    return response.data;
  },

  async deleteTask(id: number): Promise<void> {
    await api.delete(`/tasks/${id}`);
  },

  async getTaskStats(): Promise<TaskStats> {
    const response = await api.get<TaskStats>('/tasks/stats');
    return response.data;
  },
};

export default api;