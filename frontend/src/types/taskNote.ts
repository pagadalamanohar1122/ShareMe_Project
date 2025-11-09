export interface TaskNote {
  id?: number;
  taskId: number | null;
  taskTitle?: string | null;
  noteName?: string;
  noteContent: string;
  reminderTags: string[];
  createdAt?: string;
  updatedAt?: string;
}

export interface TaskNoteRequest {
  taskId: number | null;
  noteName?: string;
  noteContent: string;
  reminderTags: string[];
}

export interface TaskNoteResponse {
  id: number;
  taskId: number | null;
  taskTitle: string | null;
  noteName?: string;
  noteContent: string;
  reminderTags: string[];
  createdAt: string;
  updatedAt: string;
}

export interface TaskNoteStats {
  totalNotes: number;
  totalTags: number;
  recentNotes: TaskNoteResponse[];
}

// Predefined reminder tag categories for suggestions
export const REMINDER_TAG_CATEGORIES = {
  PRIORITY: ['urgent', 'important', 'high-priority', 'critical', 'asap'],
  STATUS: ['review-needed', 'in-review', 'blocked', 'waiting', 'follow-up'],
  TYPE: ['bug', 'feature', 'documentation', 'testing', 'research'],
  CONTEXT: ['meeting', 'discussion', 'deadline', 'milestone', 'dependency'],
  PERSONAL: ['remember', 'question', 'idea', 'concern', 'note']
} as const;

// Get all predefined tags in a flat array
export const PREDEFINED_TAGS = Object.values(REMINDER_TAG_CATEGORIES).flat();

// Color mapping for different tag categories
export const TAG_COLORS: Record<string, string> = {
  // Priority tags - red shades
  urgent: 'bg-red-100 text-red-800 border-red-200',
  important: 'bg-red-50 text-red-700 border-red-100',
  'high-priority': 'bg-orange-100 text-orange-800 border-orange-200',
  critical: 'bg-red-200 text-red-900 border-red-300',
  asap: 'bg-pink-100 text-pink-800 border-pink-200',
  
  // Status tags - blue shades
  'review-needed': 'bg-blue-100 text-blue-800 border-blue-200',
  'in-review': 'bg-indigo-100 text-indigo-800 border-indigo-200',
  blocked: 'bg-gray-100 text-gray-800 border-gray-200',
  waiting: 'bg-yellow-100 text-yellow-800 border-yellow-200',
  'follow-up': 'bg-purple-100 text-purple-800 border-purple-200',
  
  // Type tags - green shades
  bug: 'bg-red-50 text-red-600 border-red-100',
  feature: 'bg-green-100 text-green-800 border-green-200',
  documentation: 'bg-blue-50 text-blue-600 border-blue-100',
  testing: 'bg-cyan-100 text-cyan-800 border-cyan-200',
  research: 'bg-teal-100 text-teal-800 border-teal-200',
  
  // Context tags - amber shades
  meeting: 'bg-amber-100 text-amber-800 border-amber-200',
  discussion: 'bg-yellow-50 text-yellow-600 border-yellow-100',
  deadline: 'bg-orange-50 text-orange-600 border-orange-100',
  milestone: 'bg-emerald-100 text-emerald-800 border-emerald-200',
  dependency: 'bg-violet-100 text-violet-800 border-violet-200',
  
  // Personal tags - neutral shades
  remember: 'bg-slate-100 text-slate-800 border-slate-200',
  question: 'bg-lime-100 text-lime-800 border-lime-200',
  idea: 'bg-sky-100 text-sky-800 border-sky-200',
  concern: 'bg-rose-100 text-rose-800 border-rose-200',
  note: 'bg-gray-50 text-gray-600 border-gray-100'
};

// Default color for custom tags
export const DEFAULT_TAG_COLOR = 'bg-indigo-50 text-indigo-600 border-indigo-100';