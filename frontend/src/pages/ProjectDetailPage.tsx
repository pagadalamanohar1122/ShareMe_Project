import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Users, FileText, Calendar, Layers, Flag, Plus, Pencil, Trash } from 'lucide-react';
import { projectService, taskService, type Project } from '../services/api';
import CreateTaskModal from '../components/CreateTaskModal';
import { useAuth } from '../contexts/AuthContext';
import type { TaskResponse } from '../types/task';

const ProjectDetailPage: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [project, setProject] = useState<Project | null>(null);
  const [loading, setLoading] = useState(true);
  const [tasks, setTasks] = useState<TaskResponse | null>(null);
  const [showTaskModal, setShowTaskModal] = useState(false);
  const [editingTask, setEditingTask] = useState<any | null>(null);
  const { user } = useAuth();
  const projectId = Number(id);

  useEffect(() => {
    const load = async () => {
      try {
        if (!Number.isFinite(projectId)) return;
        const data = await projectService.getProject(projectId);
        setProject(data);
        // Fetch tasks for this project
        await refreshTasks();
      } catch (e) {
        // If not available yet, just stay with null – the page still loads
        setProject(null);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [projectId]);

  const refreshTasks = async () => {
    try {
      const taskData = await taskService.getTasks({ projectId, size: 100, page: 0 });
      setTasks(taskData);
    } catch (err) {
      console.warn('Failed to load tasks for project', err);
      setTasks(null);
    }
  };

  const handleCreateOrUpdateTask = async (taskData: any) => {
    try {
      if (editingTask) {
        await taskService.updateTask(editingTask.id, taskData);
      } else {
        await taskService.createTask(taskData);
      }
      setShowTaskModal(false);
      setEditingTask(null);
      await refreshTasks();
    } catch (err) {
      console.error('Failed to save task', err);
    }
  };

  const canCreateTasks = project?.canEdit; // owner for now
  const canEditTask = (t: any) => project?.canEdit || (user && (t.creator?.id === user.id || t.assignee?.id === user.id));
  const canDeleteTask = (t: any) => project?.canEdit || (user && t.creator?.id === user.id);

  const handleDeleteTask = async (taskId: number) => {
    if (!window.confirm('Delete this task? This cannot be undone.')) return;
    try {
      await taskService.deleteTask(taskId);
      await refreshTasks();
    } catch (err) {
      console.error('Failed to delete task', err);
    }
  };

  // Helper UI bits
  const badge = (text: string, color: string) => (
    <span className={`px-2 py-1 text-xs font-medium rounded-full ${color}`}>{text}</span>
  );
  const statusColor = (s?: string) => {
    switch (s) {
      case 'ACTIVE': return 'bg-blue-100 text-blue-700';
      case 'COMPLETED': return 'bg-green-100 text-green-700';
      case 'ON_HOLD': return 'bg-yellow-100 text-yellow-700';
      case 'ARCHIVED': return 'bg-gray-200 text-gray-700';
      default: return 'bg-gray-100 text-gray-700';
    }
  };
  const priorityColor = (p?: string) => {
    switch (p) {
      case 'URGENT': return 'bg-red-100 text-red-700';
      case 'HIGH': return 'bg-orange-100 text-orange-700';
      case 'MEDIUM': return 'bg-yellow-100 text-yellow-700';
      case 'LOW': return 'bg-green-100 text-green-700';
      default: return 'bg-gray-100 text-gray-700';
    }
  };
  const formatDate = (d?: string) => d ? new Date(d).toLocaleDateString('en-US',{year:'numeric',month:'short',day:'numeric'}) : '—';
  const progressPct = () => {
    if (!project) return 0;
    if (project.totalTasks === 0) return 0;
    return Math.round((project.completedTasks / project.totalTasks) * 100);
  };

  const statusBadgeColor = (s: string) => {
    switch (s) {
      case 'TODO': return 'bg-gray-100 text-gray-700';
      case 'IN_PROGRESS': return 'bg-blue-100 text-blue-700';
      case 'COMPLETED': return 'bg-green-100 text-green-700';
      case 'CANCELLED': return 'bg-red-100 text-red-700';
      default: return 'bg-gray-100 text-gray-700';
    }
  };
  const priorityBadgeColor = (p: string) => {
    switch (p) {
      case 'URGENT': return 'bg-red-100 text-red-700';
      case 'HIGH': return 'bg-orange-100 text-orange-700';
      case 'MEDIUM': return 'bg-yellow-100 text-yellow-700';
      case 'LOW': return 'bg-green-100 text-green-700';
      default: return 'bg-gray-100 text-gray-700';
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-6xl mx-auto">
        {/* Top navigation */}
        <div className="flex flex-wrap items-center justify-between mb-6 gap-3">
          <div className="flex gap-3">
            <button
              onClick={() => navigate('/projects')}
              className="inline-flex items-center gap-2 px-4 py-2 rounded-lg border border-gray-300 text-gray-700 hover:bg-gray-100"
            >
              <ArrowLeft className="w-4 h-4" /> Back to Projects
            </button>
            <button
              onClick={() => navigate('/dashboard')}
              className="inline-flex items-center gap-2 px-4 py-2 rounded-lg border border-indigo-200 text-indigo-700 hover:bg-indigo-50"
            >
              Dashboard
            </button>
          </div>
        </div>

        {loading ? (
          <div className="animate-pulse h-40 bg-white rounded-lg border" />
        ) : (
          <div className="space-y-6">
            {/* Primary Card */}
            <div className="bg-white rounded-lg shadow-sm border p-6">
              <div className="flex flex-wrap items-start justify-between gap-4 mb-4">
                <div>
                  <h1 className="text-2xl font-semibold mb-2">{project?.name ?? `Project #${projectId}`}</h1>
                  {project?.description && (
                    <p className="text-gray-600 max-w-2xl">{project.description}</p>
                  )}
                </div>
                <div className="flex flex-wrap gap-2">
                  {badge(project?.status ?? 'UNKNOWN', statusColor(project?.status))}
                  {badge(project?.priority ?? 'NONE', priorityColor(project?.priority))}
                  {project?.currentUserRole && badge(project.currentUserRole, 'bg-indigo-100 text-indigo-700')}
                  {project?.canEdit && (
                    <button
                      onClick={() => navigate(`/projects/${project?.id}?edit=true`)}
                      className="px-2 py-1 text-xs font-medium rounded-full bg-blue-600 text-white hover:bg-blue-700"
                    >Edit</button>
                  )}
                  {project?.canDelete && (
                    <button
                      onClick={() => {
                        if (window.confirm('Delete this project? This cannot be undone.')) {
                          projectService.deleteProject(project.id).then(() => navigate('/projects'));
                        }
                      }}
                      className="px-2 py-1 text-xs font-medium rounded-full bg-red-600 text-white hover:bg-red-700"
                    >Delete</button>
                  )}
                </div>
              </div>

              {/* Progress */}
              <div className="mb-4">
                <div className="flex justify-between text-sm mb-1">
                  <span className="font-medium text-gray-700">Progress</span>
                  <span className="text-gray-600">{project?.completedTasks}/{project?.totalTasks} tasks</span>
                </div>
                <div className="w-full h-3 bg-gray-200 rounded-full overflow-hidden">
                  <div className="h-3 bg-indigo-600 transition-all" style={{width: `${progressPct()}%`}} />
                </div>
                <p className="text-xs text-gray-500 mt-1">{progressPct()}% complete</p>
              </div>

              {/* Meta Info */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mt-4">
                <div className="p-3 rounded-lg bg-gray-50 border">
                  <div className="flex items-center gap-2 text-sm font-medium text-gray-700 mb-1"><Users className="w-4 h-4"/>Members</div>
                  <p className="text-gray-600 text-sm">{project?.members?.length ?? 0}</p>
                </div>
                <div className="p-3 rounded-lg bg-gray-50 border">
                  <div className="flex items-center gap-2 text-sm font-medium text-gray-700 mb-1"><FileText className="w-4 h-4"/>Documents</div>
                  <p className="text-gray-600 text-sm">{project?.documents?.length ?? 0}</p>
                </div>
                <div className="p-3 rounded-lg bg-gray-50 border">
                  <div className="flex items-center gap-2 text-sm font-medium text-gray-700 mb-1"><Layers className="w-4 h-4"/>Tasks</div>
                  <p className="text-gray-600 text-sm">{project?.totalTasks ?? 0}</p>
                </div>
              </div>

              {/* Dates */}
              <div className="mt-6 flex flex-wrap gap-6 text-sm text-gray-600">
                <div className="flex items-center gap-1"><Calendar className="w-4 h-4"/>Created: {formatDate(project?.createdAt)}</div>
                {project?.deadline && <div className="flex items-center gap-1"><Calendar className="w-4 h-4"/>Deadline: {formatDate(project.deadline)}</div>}
                {project?.updatedAt && <div className="flex items-center gap-1"><Calendar className="w-4 h-4"/>Updated: {formatDate(project.updatedAt)}</div>}
              </div>
            </div>

            {/* Members */}
            <div className="bg-white rounded-lg shadow-sm border p-6">
              <h2 className="text-lg font-semibold mb-4 flex items-center gap-2"><Users className="w-5 h-5"/>Members</h2>
              {project?.members?.length ? (
                <ul className="space-y-2">
                  {project.members.map(m => (
                    <li key={m.id} className="flex justify-between items-center text-sm">
                      <span>{m.firstName} {m.lastName}</span>
                      <span className="text-gray-500">{m.email}</span>
                    </li>
                  ))}
                </ul>
              ) : <p className="text-gray-500 text-sm">No members added yet.</p>}
            </div>

            {/* Documents */}
            <div className="bg-white rounded-lg shadow-sm border p-6">
              <h2 className="text-lg font-semibold mb-4 flex items-center gap-2"><FileText className="w-5 h-5"/>Documents</h2>
              {project?.documents?.length ? (
                <ul className="space-y-2">
                  {project.documents.map(d => (
                    <li key={d.id} className="flex justify-between items-center text-sm">
                      <span>{d.name}</span>
                      <a href={d.url} target="_blank" rel="noopener noreferrer" className="text-indigo-600 hover:underline">View</a>
                    </li>
                  ))}
                </ul>
              ) : <p className="text-gray-500 text-sm">No documents uploaded.</p>}
            </div>
          </div>
        )}
        {/* Tasks Section */}
        {tasks && (
          <div className="mt-6 bg-white rounded-lg shadow-sm border p-6">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-lg font-semibold flex items-center gap-2">
                <Layers className="w-5 h-5" /> Project Tasks ({tasks.totalElements ?? tasks.content?.length ?? 0})
              </h2>
              {canCreateTasks && (
                <button
                  onClick={() => { setEditingTask(null); setShowTaskModal(true); }}
                  className="inline-flex items-center gap-2 px-3 py-2 rounded-md bg-indigo-600 text-white text-sm hover:bg-indigo-700"
                >
                  <Plus className="w-4 h-4" /> New Task
                </button>
              )}
            </div>
            {tasks.content && tasks.content.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {tasks.content.map(t => (
                  <div key={t.id} className="border rounded-lg p-4 hover:shadow-md transition-shadow relative group">
                    {(canEditTask(t) || canDeleteTask(t)) && (
                      <div className="absolute top-2 right-2 flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                        {canEditTask(t) && (
                          <button
                            onClick={() => { setEditingTask(t); setShowTaskModal(true); }}
                            className="p-1 rounded bg-blue-600 text-white hover:bg-blue-700"
                            title="Edit Task"
                          >
                            <Pencil className="w-3 h-3" />
                          </button>
                        )}
                        {canDeleteTask(t) && (
                          <button
                            onClick={() => handleDeleteTask(t.id)}
                            className="p-1 rounded bg-red-600 text-white hover:bg-red-700"
                            title="Delete Task"
                          >
                            <Trash className="w-3 h-3" />
                          </button>
                        )}
                      </div>
                    )}
                    <div className="flex justify-between items-start gap-2">
                      <h3 className="font-medium text-gray-900 truncate" title={t.title}>{t.title}</h3>
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${statusBadgeColor(t.status)}`}>{t.status}</span>
                    </div>
                    {t.description && <p className="text-sm text-gray-600 mt-1 line-clamp-2">{t.description}</p>}
                    <div className="flex flex-wrap items-center gap-2 mt-3">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${priorityBadgeColor(t.priority)}`}>{t.priority}</span>
                      {t.dueDate && <span className="px-2 py-0.5 rounded-full text-xs bg-indigo-50 text-indigo-700 flex items-center gap-1"><Calendar className="w-3 h-3" /> {formatDate(t.dueDate)}</span>}
                      {t.assignee && <span className="px-2 py-0.5 rounded-full text-xs bg-gray-100 text-gray-700 flex items-center gap-1"><Users className="w-3 h-3" /> {t.assignee.firstName}</span>}
                    </div>
                    <div className="mt-3 text-xs text-gray-500 flex items-center gap-1">
                      <Flag className="w-3 h-3" /> {t.project?.name ?? project?.name}
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-sm text-gray-500">No tasks in this project yet.</p>
            )}
          </div>
        )}
        {showTaskModal && (
          <CreateTaskModal
            isOpen={showTaskModal}
            onClose={() => { setShowTaskModal(false); setEditingTask(null); }}
            onSubmit={handleCreateOrUpdateTask}
            editingTask={editingTask}
            fixedProjectId={project?.id}
            fixedProjectName={project?.name}
          />
        )}
      </div>
    </div>
  );
};

export default ProjectDetailPage;
