const Rels = {
  SELF: 'self',
  ITEM: 'item',
  HOME: 'home',
  USER_HOME: 'user-home',
  REGISTER: 'register',
  LOGIN: 'login',
  LOGOUT: 'logout',
  USER: 'get-user',
  USERS: 'get-users',
  JOIN_LOBBY: 'join-lobby',
  FIND_MATCH: 'find-match',
  LEAVE_LOBBY: 'leave-lobby',
  GAMES: 'get-games',
  GAME: 'get-game',
  PLAY_GAME: 'play-game',
  LEAVE_GAME: 'leave-game',
  OPPONENT: 'get-opponent',
  BLACK_PLAYER: 'get-black-player',
  WHITE_PLAYER: 'get-white-player',
} as const;

export default Rels;
