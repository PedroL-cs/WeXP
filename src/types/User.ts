export type PublicUser = {
  publicId: string;
  username: string;
  bio?: string;
  avatar?: string;
};

export type CurrentUser = PublicUser & {
  login: string;
  email: string;
  birthDate?: string;
};

export type UpdateUserData = {
  username?: string;
  bio?: string;
  birthDate?: string;
};
