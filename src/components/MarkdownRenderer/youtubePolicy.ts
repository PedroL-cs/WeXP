const VIDEO_ID_PATTERN = /^[A-Za-z0-9_-]{11}$/;
const YOUTUBE_HOSTS = new Set([
  'youtube.com',
  'www.youtube.com',
  'm.youtube.com',
  'youtu.be',
  'www.youtu.be',
  'youtube-nocookie.com',
  'www.youtube-nocookie.com',
]);

export function isValidYouTubeVideoId(value: unknown): value is string {
  return typeof value === 'string' && VIDEO_ID_PATTERN.test(value);
}

export function extractYouTubeVideoId(value: string): string | null {
  const input = value.trim();
  if (
    !input ||
    /\s|\\/.test(input) ||
    Array.from(input).some(
      character =>
        character.charCodeAt(0) < 32 || character.charCodeAt(0) === 127,
    )
  ) {
    return null;
  }

  try {
    const url = new URL(input);
    const hostname = url.hostname.toLowerCase();
    if (
      url.protocol !== 'https:' ||
      !YOUTUBE_HOSTS.has(hostname) ||
      url.username ||
      url.password
    ) {
      return null;
    }

    let videoId: string | null = null;
    const segments = url.pathname.split('/').filter(Boolean);

    if (hostname === 'youtu.be' || hostname === 'www.youtu.be') {
      videoId = segments.length === 1 ? segments[0] : null;
    } else if (url.pathname === '/watch') {
      videoId = url.searchParams.get('v');
    } else if (
      segments.length === 2 &&
      (segments[0] === 'shorts' || segments[0] === 'embed')
    ) {
      videoId = segments[1];
    }

    return isValidYouTubeVideoId(videoId) ? videoId : null;
  } catch {
    return null;
  }
}

export function getYouTubeWatchUrl(videoId: string) {
  return `https://www.youtube.com/watch?v=${encodeURIComponent(videoId)}`;
}

export function getYouTubeEmbedUrl(videoId: string) {
  return `https://www.youtube-nocookie.com/embed/${encodeURIComponent(videoId)}`;
}
