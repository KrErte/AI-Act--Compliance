import { Injectable, signal, computed } from '@angular/core';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
  id: number;
  type: ToastType;
  message: string;
  duration: number;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private nextId = 0;
  private readonly _toasts = signal<Toast[]>([]);

  readonly toasts = computed(() => this._toasts());

  success(message: string, duration = 4000): void {
    this.add('success', message, duration);
  }

  error(message: string, duration = 6000): void {
    this.add('error', message, duration);
  }

  warning(message: string, duration = 5000): void {
    this.add('warning', message, duration);
  }

  info(message: string, duration = 4000): void {
    this.add('info', message, duration);
  }

  remove(id: number): void {
    this._toasts.update(toasts => toasts.filter(t => t.id !== id));
  }

  private add(type: ToastType, message: string, duration: number): void {
    const id = this.nextId++;
    const toast: Toast = { id, type, message, duration };
    this._toasts.update(toasts => [...toasts, toast]);

    if (duration > 0) {
      setTimeout(() => this.remove(id), duration);
    }
  }
}
