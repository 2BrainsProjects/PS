import { Problem } from '../../services/media/problem/Problem';

const MIN_USERNAME_LEN = 3;
const MAX_USERNAME_LEN = 20;
const MIN_EMAIL_LEN = 3;
const MAX_EMAIL_LEN = 30;
const MIN_PASSWORD_LEN = 8;
const MAX_PASSWORD_LEN = 30;

export function validateLogin(usernameOrEmail: string, password: string): Problem | undefined {
  if (usernameOrEmail === '' || password === '') {
    return { title: 'Invalid Input', detail: 'Please fill all fields' };
  }
}

export function validateRegister(
  username: string,
  email: string,
  password: string,
  confirmPassword: string
): Problem | undefined {
  if (username === '' || email === '' || password === '' || confirmPassword === '') {
    return { title: 'Invalid Input', detail: 'Please fill all fields' };
  }
  if (username.length < MIN_USERNAME_LEN || username.length > MAX_USERNAME_LEN) {
    return {
      title: 'Invalid Input',
      detail: `Username must be between ${MIN_USERNAME_LEN} and ${MAX_USERNAME_LEN} characters`,
    };
  }
  if (email.length < MIN_EMAIL_LEN || email.length > MAX_EMAIL_LEN) {
    return {
      title: 'Invalid Input',
      detail: `Email must be between ${MIN_EMAIL_LEN} and ${MAX_EMAIL_LEN} characters`,
    };
  }
  if (password.length < MIN_PASSWORD_LEN || password.length > MAX_PASSWORD_LEN) {
    return {
      title: 'Invalid Input',
      detail: `Password must be between ${MIN_PASSWORD_LEN} and ${MAX_PASSWORD_LEN} characters`,
    };
  }
  if (!email.includes('@')) {
    return { title: 'Invalid Input', detail: 'Invalid email' };
  }
  if (password !== confirmPassword) {
    return { title: 'Invalid Input', detail: 'Passwords do not match' };
  }
}
