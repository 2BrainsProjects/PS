export class LoginOutputModel {
  constructor(
    public token: string,
    public expiresIn: number
  ) {}
}
