import {
  CameraIcon,
  CheckIcon,
  EnvelopeSimpleIcon,
  PencilSimpleIcon,
  QuotesIcon,
  UserCircleIcon,
} from '@phosphor-icons/react';
import {
  type ChangeEvent,
  type FormEvent,
  useEffect,
  useMemo,
  useState,
} from 'react';
import { useParams } from 'react-router';
import {
  getCurrentUser,
  getPublicUser,
  updateCurrentUser,
} from '../../api/users';
import type { CurrentUser, PublicUser } from '../../types/User';
import styles from './styles.module.css';

type FormValues = {
  username: string;
  bio: string;
  birthDate: string;
};

function toInputDate(value?: string) {
  if (!value || !value.includes('/')) return value ?? '';
  const [day, month, year] = value.split('/');
  return `${year}-${month}-${day}`;
}

function toApiDate(value: string) {
  if (!value || !value.includes('-')) return value;
  const [year, month, day] = value.split('-');
  return `${day}/${month}/${year}`;
}

function UserProfilePage() {
  const { userId } = useParams();
  const isOwnProfile = !userId;
  const [user, setUser] = useState<CurrentUser | PublicUser | null>(null);
  const [form, setForm] = useState<FormValues>({
    username: '',
    bio: '',
    birthDate: '',
  });
  const [avatar, setAvatar] = useState<File>();
  const [editing, setEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    async function load() {
      try {
        const profile = isOwnProfile
          ? await getCurrentUser()
          : await getPublicUser(userId);
        setUser(profile);
        setForm({
          username: profile.username,
          bio: profile.bio ?? '',
          birthDate: isOwnProfile
            ? toInputDate((profile as CurrentUser).birthDate)
            : '',
        });
      } catch (caughtError) {
        setError(
          caughtError instanceof Error
            ? caughtError.message
            : 'Não foi possível carregar o perfil.',
        );
      } finally {
        setLoading(false);
      }
    }

    load();
  }, [isOwnProfile, userId]);

  // Prévia local da nova foto antes de salvar
  const avatarPreview = useMemo(
    () => (avatar ? URL.createObjectURL(avatar) : undefined),
    [avatar],
  );

  useEffect(() => {
    return () => {
      if (avatarPreview) URL.revokeObjectURL(avatarPreview);
    };
  }, [avatarPreview]);

  function chooseAvatar(event: ChangeEvent<HTMLInputElement>) {
    setAvatar(event.target.files?.[0]);
  }

  async function save(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!user || !isOwnProfile) return;

    try {
      setSaving(true);
      setError('');
      const updated = await updateCurrentUser(
        {
          username: form.username.trim(),
          bio: form.bio.trim(),
          birthDate: toApiDate(form.birthDate),
        },
        avatar,
      );
      setUser(updated);
      setAvatar(undefined);
      setEditing(false);
      setSuccess('Perfil atualizado com sucesso.');
      window.dispatchEvent(new Event('user-profile-updated'));
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : 'Não foi possível salvar o perfil.',
      );
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <main className={styles.page}>
        <p className={styles.status}>Carregando perfil...</p>
      </main>
    );
  }

  if (!user) {
    return (
      <main className={styles.page}>
        <p className={styles.status}>{error}</p>
      </main>
    );
  }

  return (
    <main className={styles.page}>
      <section className={styles.card}>
        <div className={styles.banner} aria-hidden='true' />

        <div className={styles.hero}>
          <div className={styles.avatar}>
            {avatarPreview || user.avatar ? (
              <img src={avatarPreview ?? user.avatar} alt='' />
            ) : (
              <UserCircleIcon size={96} weight='thin' />
            )}
          </div>
          <div className={styles.identity}>
            <p className={styles.eyebrow}>
              {isOwnProfile ? 'Meu perfil' : 'Perfil de usuário'}
            </p>
            <h1>{user.username}</h1>
            {isOwnProfile && (
              <span className={styles.login}>
                @{(user as CurrentUser).login}
              </span>
            )}
          </div>
          {isOwnProfile && !editing && (
            <button
              type='button'
              className={styles.edit}
              onClick={() => setEditing(true)}
            >
              <PencilSimpleIcon size={19} /> Editar perfil
            </button>
          )}
        </div>

        {success && <p className={styles.success}>{success}</p>}
        {error && <p className={styles.error}>{error}</p>}

        {isOwnProfile && editing ? (
          <form className={styles.form} onSubmit={save}>
            <label className={styles.avatarPicker}>
              <CameraIcon size={22} />
              <span>
                Foto de perfil
                <small>
                  {avatar
                    ? avatar.name
                    : 'Clique para enviar uma imagem e substituir o avatar.'}
                </small>
              </span>
              <input type='file' accept='image/*' onChange={chooseAvatar} />
            </label>
            <label>
              Nome de exibição
              <input
                value={form.username}
                maxLength={100}
                required
                onChange={event =>
                  setForm({ ...form, username: event.target.value })
                }
              />
            </label>
            <label>
              Biografia
              <textarea
                value={form.bio}
                maxLength={500}
                rows={5}
                onChange={event =>
                  setForm({ ...form, bio: event.target.value })
                }
              />
            </label>
            <label>
              Data de nascimento
              <input
                type='date'
                value={form.birthDate}
                onChange={event =>
                  setForm({ ...form, birthDate: event.target.value })
                }
              />
            </label>
            <div className={styles.actions}>
              <button
                type='button'
                className={styles.cancel}
                onClick={() => {
                  setEditing(false);
                  setAvatar(undefined);
                }}
              >
                Cancelar
              </button>
              <button type='submit' className={styles.save} disabled={saving}>
                <CheckIcon size={20} />
                {saving ? 'Salvando...' : 'Salvar alterações'}
              </button>
            </div>
          </form>
        ) : (
          <div className={styles.details}>
            <div className={styles.detail}>
              <h2>
                <QuotesIcon size={16} weight='fill' /> Sobre
              </h2>
              <p className={user.bio ? '' : styles.placeholder}>
                {user.bio || 'Este usuário ainda não escreveu uma biografia.'}
              </p>
            </div>
            {isOwnProfile && (
              <div className={styles.detail}>
                <h2>
                  <EnvelopeSimpleIcon size={16} weight='fill' /> E-mail
                </h2>
                <p>{(user as CurrentUser).email}</p>
              </div>
            )}
          </div>
        )}
      </section>
    </main>
  );
}

export default UserProfilePage;
