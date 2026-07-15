import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Question, QuestionPage, QuestionQuery, QuestionRequest } from './question.model';

@Injectable({ providedIn: 'root' })
export class QuestionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/questions';

  findAll(query: QuestionQuery): Observable<QuestionPage> {
    let params = new HttpParams().set('page', query.page).set('size', query.size);

    if (query.search?.trim()) {
      params = params.set('search', query.search.trim());
    }
    if (query.difficulty) {
      params = params.set('difficulty', query.difficulty);
    }
    if (query.status) {
      params = params.set('status', query.status);
    }

    return this.http.get<QuestionPage>(this.baseUrl, { params });
  }

  create(request: QuestionRequest): Observable<Question> {
    return this.http.post<Question>(this.baseUrl, request);
  }

  update(id: number, request: QuestionRequest): Observable<Question> {
    return this.http.put<Question>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
