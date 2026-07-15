export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD';
export type AnswerStatus = 'DRAFT' | 'REVIEWED';

export interface Question {
  id: number;
  title: string;
  answer: string;
  answerStatus: AnswerStatus;
  topic: string;
  difficulty: Difficulty;
  tags: string[];
}

export interface QuestionRequest {
  title: string;
  answer: string;
  answerStatus: AnswerStatus;
  topic: string;
  difficulty: Difficulty;
  tags: string[];
}

export interface QuestionPage {
  content: Question[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface QuestionQuery {
  search?: string;
  difficulty?: Difficulty;
  status?: AnswerStatus;
  page: number;
  size: number;
}

export const difficulties: Difficulty[] = ['EASY', 'MEDIUM', 'HARD'];
export const answerStatuses: AnswerStatus[] = ['DRAFT', 'REVIEWED'];
