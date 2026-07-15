import { Component, computed, inject, signal } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgClass } from '@angular/common';
import { finalize, startWith } from 'rxjs';
import { marked } from 'marked';
import {
  AnswerStatus,
  Difficulty,
  Question,
  QuestionRequest,
  answerStatuses,
  difficulties,
} from './question.model';
import { QuestionService } from './question.service';

type StatusFilter = AnswerStatus | 'ALL';
type DifficultyFilter = Difficulty | 'ALL';

@Component({
  selector: 'app-root',
  imports: [ReactiveFormsModule, NgClass],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly questionService = inject(QuestionService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly sanitizer = inject(DomSanitizer);

  readonly difficulties = difficulties;
  readonly answerStatuses = answerStatuses;
  readonly questions = signal<Question[]>([]);
  readonly totalElements = signal(0);
  readonly totalPages = signal(0);
  readonly page = signal(0);
  readonly pageSize = signal(20);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly selectedQuestion = signal<Question | null>(null);
  readonly searchTerm = signal('');
  readonly difficultyFilter = signal<DifficultyFilter>('ALL');
  readonly statusFilter = signal<StatusFilter>('ALL');
  readonly answerMarkdown = signal('');

  readonly form = this.formBuilder.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(500)]],
    topic: ['', [Validators.required, Validators.maxLength(100)]],
    difficulty: ['EASY' as Difficulty, Validators.required],
    answerStatus: ['DRAFT' as AnswerStatus, Validators.required],
    tags: [''],
    answer: ['', Validators.required],
  });

  readonly reviewedCount = computed(
    () => this.questions().filter((question) => question.answerStatus === 'REVIEWED').length,
  );

  readonly draftCount = computed(
    () => this.questions().filter((question) => question.answerStatus === 'DRAFT').length,
  );

  readonly pageStart = computed(() =>
    this.totalElements() === 0 ? 0 : this.page() * this.pageSize() + 1,
  );

  readonly pageEnd = computed(() =>
    Math.min((this.page() + 1) * this.pageSize(), this.totalElements()),
  );

  readonly answerPreview = computed<SafeHtml>(() => {
    const markdown = this.answerMarkdown().trim();
    const rendered = marked.parse(markdown || '_Answer preview will appear here._', {
      async: false,
      breaks: true,
      gfm: true,
    });

    return this.sanitizer.bypassSecurityTrustHtml(rendered);
  });

  constructor() {
    this.form.controls.answer.valueChanges
      .pipe(startWith(this.form.controls.answer.value))
      .subscribe((answer) => this.answerMarkdown.set(answer));
    this.loadQuestions();
  }

  loadQuestions(): void {
    this.loading.set(true);
    this.error.set('');
    const difficulty = this.difficultyFilter();
    const status = this.statusFilter();

    this.questionService
      .findAll({
        search: this.searchTerm(),
        difficulty: difficulty === 'ALL' ? undefined : difficulty,
        status: status === 'ALL' ? undefined : status,
        page: this.page(),
        size: this.pageSize(),
      })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (page) => {
          this.questions.set(page.content);
          this.totalElements.set(page.totalElements);
          this.totalPages.set(page.totalPages);
          this.page.set(page.page);
          this.pageSize.set(page.size);
        },
        error: () => this.error.set('Unable to load questions. Check that the backend is running.'),
      });
  }

  selectQuestion(question: Question): void {
    this.selectedQuestion.set(question);
    this.error.set('');
    this.form.setValue({
      title: question.title,
      topic: question.topic,
      difficulty: question.difficulty,
      answerStatus: question.answerStatus,
      tags: question.tags.join(', '),
      answer: question.answer,
    });
  }

  newQuestion(): void {
    this.selectedQuestion.set(null);
    this.error.set('');
    this.form.reset({
      title: '',
      topic: '',
      difficulty: 'EASY',
      answerStatus: 'DRAFT',
      tags: '',
      answer: '',
    });
  }

  saveQuestion(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const selectedQuestion = this.selectedQuestion();
    const request = this.toRequest();
    const saveOperation = selectedQuestion
      ? this.questionService.update(selectedQuestion.id, request)
      : this.questionService.create(request);

    this.saving.set(true);
    this.error.set('');

    saveOperation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: (savedQuestion) => {
        if (!selectedQuestion) {
          this.page.set(0);
        }
        this.selectQuestion(savedQuestion);
        this.loadQuestions();
      },
      error: () => this.error.set('Unable to save the question. Review the form and try again.'),
    });
  }

  deleteSelectedQuestion(): void {
    const selectedQuestion = this.selectedQuestion();
    if (!selectedQuestion) {
      return;
    }

    this.saving.set(true);
    this.error.set('');

    this.questionService
      .delete(selectedQuestion.id)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.newQuestion();
          this.loadQuestions();
        },
        error: () => this.error.set('Unable to delete the selected question.'),
      });
  }

  updateSearchTerm(value: string): void {
    this.searchTerm.set(value);
    this.page.set(0);
    this.loadQuestions();
  }

  updateDifficultyFilter(value: string): void {
    this.difficultyFilter.set(value as DifficultyFilter);
    this.page.set(0);
    this.loadQuestions();
  }

  updateStatusFilter(value: string): void {
    this.statusFilter.set(value as StatusFilter);
    this.page.set(0);
    this.loadQuestions();
  }

  updatePageSize(value: string): void {
    this.pageSize.set(Number(value));
    this.page.set(0);
    this.loadQuestions();
  }

  goToPreviousPage(): void {
    if (this.page() === 0) {
      return;
    }
    this.page.update((page) => page - 1);
    this.loadQuestions();
  }

  goToNextPage(): void {
    if (this.page() + 1 >= this.totalPages()) {
      return;
    }
    this.page.update((page) => page + 1);
    this.loadQuestions();
  }

  private toRequest(): QuestionRequest {
    const value = this.form.getRawValue();

    return {
      title: value.title.trim(),
      topic: value.topic.trim(),
      difficulty: value.difficulty,
      answerStatus: value.answerStatus,
      answer: value.answer.trim(),
      tags: value.tags
        .split(',')
        .map((tag) => tag.trim())
        .filter(Boolean),
    };
  }
}
