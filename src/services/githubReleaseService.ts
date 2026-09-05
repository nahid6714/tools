import { AppReleaseInfo } from '../types/portfolio';

// In-memory cache to prevent hitting GitHub API rate limits
const cache = new Map<string, { data: AppReleaseInfo; timestamp: number }>();
const CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes cache

/**
 * Parses markdown release body into clean bullet points
 */
function parseReleaseNotes(body: string | undefined): string[] {
  if (!body) return ['General improvements and bug fixes.'];
  
  const lines = body
    .split('\n')
    .map(line => line.trim())
    .filter(line => line.length > 0 && !line.startsWith('##') && !line.startsWith('**Build Number'));

  const cleanNotes: string[] = [];
  for (const line of lines) {
    // Remove markdown list markers and bold tags
    let cleaned = line.replace(/^[-*•]\s*/, '').trim();
    cleaned = cleaned.replace(/\*\*(.*?)\*\*/g, '$1');
    if (cleaned.length > 0) {
      cleanNotes.push(cleaned);
    }
  }

  return cleanNotes.length > 0 ? cleanNotes.slice(0, 6) : ['Continuous stability and maintenance updates.'];
}

/**
 * Formats ISO date string to readable format e.g. "Aug 18, 2026"
 */
function formatDate(dateStr: string): string {
  try {
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  } catch {
    return dateStr;
  }
}

/**
 * Fetches the latest release for a GitHub repository
 * Automatically extracts the .apk file asset, size, version, and changelog.
 */
export async function fetchLatestRelease(
  owner: string,
  repo: string,
  fallback: AppReleaseInfo
): Promise<{ release: AppReleaseInfo; isLive: boolean; error?: string }> {
  const cacheKey = `${owner}/${repo}`;
  const cached = cache.get(cacheKey);

  if (cached && Date.now() - cached.timestamp < CACHE_TTL_MS) {
    return { release: cached.data, isLive: true };
  }

  try {
    const response = await fetch(`https://api.github.com/repos/${owner}/${repo}/releases/latest`, {
      headers: {
        Accept: 'application/vnd.github.v3+json',
      },
    });

    if (!response.ok) {
      if (response.status === 404) {
        // Repository has no published releases yet
        return { release: fallback, isLive: false, error: 'No releases published yet' };
      }
      if (response.status === 403) {
        // Rate limit reached
        return { release: fallback, isLive: false, error: 'GitHub API rate limit reached' };
      }
      return { release: fallback, isLive: false, error: `GitHub API status ${response.status}` };
    }

    const data = await response.json();
    
    // Find APK asset
    const assets = Array.isArray(data.assets) ? data.assets : [];
    const apkAsset = assets.find((asset: { name?: string; content_type?: string }) => {
      const name = asset.name?.toLowerCase() || '';
      return name.endsWith('.apk') || asset.content_type === 'application/vnd.android.package-archive';
    });

    const releaseDate = data.published_at ? formatDate(data.published_at) : fallback.releaseDate;
    const version = data.tag_name || data.name || fallback.version;
    const whatsNew = parseReleaseNotes(data.body);

    let apkSize = fallback.apkSize;
    let apkFileName = fallback.apkFileName;
    let downloadUrl = fallback.downloadUrl;

    if (apkAsset) {
      const bytes = apkAsset.size || 0;
      apkSize = bytes > 0 ? `${(bytes / (1024 * 1024)).toFixed(2)} MB` : fallback.apkSize;
      apkFileName = apkAsset.name || fallback.apkFileName;
      downloadUrl = apkAsset.browser_download_url || fallback.downloadUrl;
    } else if (data.html_url) {
      downloadUrl = data.html_url;
    }

    const liveRelease: AppReleaseInfo = {
      version,
      releaseDate,
      apkSize,
      apkFileName,
      downloadUrl,
      whatsNew,
      tagCommit: data.target_commitish ? data.target_commitish.slice(0, 7) : fallback.tagCommit,
    };

    cache.set(cacheKey, { data: liveRelease, timestamp: Date.now() });
    return { release: liveRelease, isLive: true };
  } catch (err) {
    console.warn(`[GitHub Service] Failed to fetch release for ${owner}/${repo}, using fallback:`, err);
    return { release: fallback, isLive: false, error: 'Network unavailable' };
  }
}

/**
 * Triggers direct APK download in browser without redirecting visitor to GitHub webpage
 */
export function triggerDirectApkDownload(downloadUrl: string, fileName: string): boolean {
  try {
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.setAttribute('download', fileName);
    link.setAttribute('rel', 'noopener noreferrer');
    link.setAttribute('target', '_blank');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    return true;
  } catch (err) {
    console.error('Failed to trigger direct download:', err);
    window.location.href = downloadUrl;
    return false;
  }
}
