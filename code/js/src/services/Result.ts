import { Problem } from './media/problem/Problem';

/**
 * Union that encapsulates a successful outcome with a value of type T or a failure with a `Problem`
 * @typeParam T - The type of the value
 * @see Problem
 * @see runCatching
 */
export class Result<T> {
  constructor(public readonly value: T | Problem) {}

  getValue(): T {
    if (this.isFailure()) {
      throw new Error('Cannot get the value of a failure');
    }
    return this.value as T;
  }

  getProblem(): Problem {
    if (this.isSuccess()) {
      throw new Error('Cannot get the problem of a success');
    }
    return this.value as Problem;
  }

  getValueOrNull(): T | null {
    if (this.isFailure()) {
      return null;
    }
    return this.value as T;
  }

  isSuccess(): boolean {
    return !(this.value instanceof Problem);
  }

  isFailure(): boolean {
    return this.value instanceof Problem;
  }

  onSuccess(callback: (value: T) => void): Result<T> {
    if (this.isSuccess()) {
      callback(this.value as T);
    }
    return this;
  }

  onFailure(callback: (problem: Problem) => void): Result<T> {
    if (this.isFailure()) {
      callback(this.value as Problem);
    }
    return this;
  }

  map<U>(f: (value: T) => U): Result<U> {
    if (this.isFailure()) {
      return new Result<U>(this.value as Problem);
    }
    return new Result<U>(f(this.value as T));
  }

  mapVoid(): Result<void> {
    return this.map(() => {});
  }
}

/**
 * Resolves a given promise and returns its encapsulated result
 * if invocation was successful, catching any error that was thrown
 * from the promise execution and encapsulating it as a failure in the form of a `Problem`.
 * @param promise - A promise to be executed and wrapped in a `Result`.
 * @returns A `Result` containing either the resolved value or a problem.
 */
export async function runCatching<T>(promise: Promise<T>): Promise<Result<T>> {
  try {
    const result = await promise;
    return new Result(result);
  } catch (e) {
    console.error(e);
    if (e instanceof Problem) {
      return new Result<T>(e);
    }
    const problem = new Problem(undefined, 'Error', (e as Error).message);
    return new Result<T>(problem);
  }
}
